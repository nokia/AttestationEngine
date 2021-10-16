/*
 * Copyright 2021 Nokia
 * Licensed under the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 */

/* SPDX-License-Identifier: BSD-3-Clause */
#define _GNU_SOURCE
#include <errno.h>
#include <getopt.h>
#include <inttypes.h>
#include <signal.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <arpa/inet.h>

#include <tss2/tss2_rc.h>
#include <tss2/tss2_esys.h>
#include <tss2/tss2_tctildr.h>

typedef enum tool_rc tool_rc;
enum tool_rc {
    /* do not reorder or change, part of returned codes to exit */
    /* maps to common/returns.md */
    tool_rc_success = 0,
    tool_rc_general_error,
    tool_rc_option_error,
    tool_rc_auth_error,
    tool_rc_tcti_error,
    tool_rc_unsupported
};

#define TPM2_COMMAND_HEADER_SIZE  (sizeof(tpm2_command_header))
#define TPM2_RESPONSE_HEADER_SIZE (sizeof(tpm2_response_header))

#define TPM2_MAX_SIZE 4096

#define TPM2_COMMAND_HEADER_SIZE  (sizeof(tpm2_command_header))
#define TPM2_RESPONSE_HEADER_SIZE (sizeof(tpm2_response_header))

#define TPM2_MAX_SIZE 4096

typedef union tpm2_command_header tpm2_command_header;
union tpm2_command_header {
    struct {
        TPMI_ST_COMMAND_TAG tag; // uint16
        UINT32 size; //
        TPM2_CC command_code;
        UINT8 data[];
    }__attribute__((packed));
    UINT8 bytes[0];
};

typedef union tpm2_response_header tpm2_response_header;
union tpm2_response_header {
    struct {
        TPM2_ST tag;
        UINT32 size;
        TSS2_RC response_code;
        UINT8 data[];
    }__attribute__((packed));
    UINT8 bytes[0];
};

/**
 * Converts a byte-array to a tpm2_command_header struct.
 * @param h
 *  The byte array to convert to a tpm2_command_header.
 * @return
 *  A converted byte array.
 */
static inline tpm2_command_header *tpm2_command_header_from_bytes(UINT8 *h) {

    return (tpm2_command_header *) h;
}

/**
 * Converts a byte-array to a tpm2_response_header struct.
 * @param h
 *  The byte array to convert to a tpm2_response_header.
 * @return
 *  A converted byte array.
 */
static inline tpm2_response_header *tpm2_response_header_from_bytes(UINT8 *h) {

    return (tpm2_response_header *) h;
}

/**
 * Retrieves the command tag from a command converting to host
 * endianess.
 * @param command
 * @return
 */
static inline TPMI_ST_COMMAND_TAG tpm2_command_header_get_tag(
        tpm2_command_header *command) {

    return ntohs(command->tag);
}

/**
 * Retrieves the command size from a command converting to host
 * endianess.
 * @param command
 * @param include_header
 * @return
 */
static inline UINT32 tpm2_command_header_get_size(tpm2_command_header *command,
        bool include_header) {

    UINT32 size = ntohl(command->size);
    return include_header ? size : size - TPM2_COMMAND_HEADER_SIZE;
}

/**
 * Retrieves the command code from a command converting to host
 * endianess.
 * @param command
 * @return
 */
static inline TPM2_CC tpm2_command_header_get_code(tpm2_command_header *command) {

    return ntohl(command->command_code);
}

/**
 * Retrieves the response size from a response header converting to host
 * endianess.
 * @param response_header
 * @param include_header
 * @return
 */
static inline UINT32 tpm2_response_header_get_size(
        tpm2_response_header *response, bool include_header) {

    UINT32 size = ntohl(response->size);
    return include_header ? size : size - TPM2_RESPONSE_HEADER_SIZE;
}

/**
 * Retrieves the response tag from a response header converting to host
 * endianess.
 * @param response_header
 * @return
 */
static inline TPM2_ST tpm2_response_header_get_tag(
        tpm2_response_header *response) {

    return ntohs(response->tag);
}

/**
 * Retrieves the response code from a response header converting to host
 * endianess.
 * @param response_header
 * @return
 */
static inline TSS2_RC tpm2_response_header_get_code(
        tpm2_response_header *response) {

    return ntohl(response->response_code);
}

typedef struct tpm2_send_ctx tpm2_send_ctx;
struct tpm2_send_ctx {
    FILE *input;
    FILE *output;
    tpm2_command_header *command;
    TSS2_TCTI_CONTEXT *tcti;
};

typedef void (*sighandler_t)(int);

static tpm2_send_ctx ctx;

static void sig_handler(int signum) {
    (void)signum;

    exit (tool_rc_success);
}

static int read_command_from_file(FILE *f, tpm2_command_header **c,
        UINT32 *size) {

    UINT8 buffer[TPM2_COMMAND_HEADER_SIZE];

    size_t ret = fread(buffer, TPM2_COMMAND_HEADER_SIZE, 1, f);
    if (ret != 1 && ferror(f) && errno != EINTR) {
        fprintf(stderr, "Failed to read command header: %s", strerror (errno));
        return -1;
    }

    if (feof(f) || ferror(f)) {
        return 0;
    }

    tpm2_command_header *header = tpm2_command_header_from_bytes(buffer);

    UINT32 command_size = tpm2_command_header_get_size(header, true);
    UINT32 data_size = tpm2_command_header_get_size(header, false);

    if (command_size > TPM2_MAX_SIZE || command_size < data_size) {
        fprintf(stderr, "Command buffer %"PRIu32" bytes cannot be smaller then the "
                "encapsulated data %"PRIu32" bytes, and can not be bigger than"
                " the maximum buffer size", command_size, data_size);
        return -1;
    }

    tpm2_command_header *command = (tpm2_command_header *) malloc(command_size);
    if (!command) {
        fprintf(stderr, "oom");
        return -1;
    }

    /* copy the header into the struct */
    memcpy(command, buffer, sizeof(buffer));

    ret = fread(command->data, data_size, 1, f);
    if (ret != 1 && ferror(f)) {
        fprintf(stderr, "Failed to read command body: %s", strerror (errno));
        free(command);
        return -1;
    }

    *c = command;
    *size = command_size;

    return 1;
}

static bool writex(FILE *f, UINT8 *data, size_t size) {

    size_t wrote = 0;
    size_t index = 0;
    do {
        wrote = fwrite(&data[index], 1, size, f);
        if (wrote != size) {
            if (errno != EINTR) {
                return false;
            }
            /* continue on EINTR */
        }
        size -= wrote;
        index += wrote;
    } while (size > 0);

    return true;
}

static bool write_response_to_file(FILE *f, UINT8 *rbuf) {

    tpm2_response_header *r = tpm2_response_header_from_bytes(rbuf);

    UINT32 size = tpm2_response_header_get_size(r, true);

    bool rc =  writex(f, r->bytes, size);
    fflush(f);
    return rc;
}

static FILE *open_file(const char *path, const char *mode) {
    FILE *f = fopen(path, mode);
    if (!f) {
        fprintf(stderr, "Could not open \"%s\", error: \"%s\"", path, strerror(errno));
    }
    return f;
}

static void close_file(FILE *f) {

    if (f && (f != stdin || f != stdout)) {
        fclose(f);
    }
}

static void tpm2_tool_onstart(int argc, char **argv) {

    ctx.input = stdin;
    ctx.output = stdout;
    const char *tcti_conf = NULL;

    static const struct option topts[] = {
		{ "tcti"  , required_argument, NULL, 'T'  },
        { "output", required_argument, NULL, 'o'  },
		{ NULL,     0,                 NULL, '\0' }
    };

    int c;
    while ((c = getopt_long(argc, argv, "o:", topts, NULL)) != -1) {
		switch (c) {
		case 'o':
			ctx.output = open_file(optarg, "wb");
			if (!ctx.output) {
				fprintf(stderr, "Cannot open \"%s\", error: %s\n", optarg, strerror(errno));
				exit(1);
			}
			break;
		case 'T':
			tcti_conf = optarg;
			break;
		case '?':
		case ':':
			fprintf(stderr, "Missing option parameter\n");
			exit(1);
		default:
			fprintf(stderr, "Invalid option\n");
			exit(1);
		}
    }

    /* handle args */
    char **tool_argv = &argv[optind];
    int tool_argc = argc - optind;

    if (tool_argc > 1) {
    	fprintf(stderr, "Only one argument supported, got: %d\n", tool_argc);
    	exit(1);
    } else if (tool_argc == 1) {
		ctx.input = open_file(tool_argv[0], "rb");
		if (!ctx.input) {
			fprintf(stderr, "Cannot open \"%s\", error: %s\n", optarg, strerror(errno));
			exit(1);
		}
    }

	TSS2_RC rv = Tss2_TctiLdr_Initialize(tcti_conf, &ctx.tcti);
	if (rv != TSS2_RC_SUCCESS) {
		fprintf(stderr, "Could not load tcti, got: \"%s\"", Tss2_RC_Decode(rv));
		exit(1);
	}

	return;
}

/*
 * This program reads a TPM command buffer from stdin then dumps it out
 * to a tabd TCTI. It then reads the response from the TCTI and writes it
 * to stdout. Like the TCTI, we expect the input TPM command buffer to be
 * in network byte order (big-endian). We output the response in the same
 * form.
 */
static void tpm2_tool_onrun(TSS2_TCTI_CONTEXT *tcti_context) {

    sighandler_t old_handler = signal(SIGINT, sig_handler);
    if(old_handler == SIG_ERR) {
        fprintf(stderr, "WARNING: Could not set SIGINT handler: %s", strerror(errno));
    }

    while (1) {
        UINT32 size;
        int result = read_command_from_file(ctx.input, &ctx.command, &size);
        if (result < 0) {
            fprintf(stderr, "failed to read TPM2 command buffer from file");
            exit(1);
        } else if (result == 0) {
            exit(0);
        }

        TSS2_RC rval = Tss2_Tcti_Transmit(tcti_context, size, ctx.command->bytes);
        if (rval != TPM2_RC_SUCCESS) {
            fprintf(stderr, "tss2_tcti_transmit failed: %s", Tss2_RC_Decode(rval));
            exit(1);
        }

        size_t rsize = TPM2_MAX_SIZE;
        UINT8 rbuf[TPM2_MAX_SIZE];
        rval = Tss2_Tcti_Receive(tcti_context, &rsize, rbuf,
                TSS2_TCTI_TIMEOUT_BLOCK);
        if (rval != TPM2_RC_SUCCESS) {
            fprintf(stderr, "tss2_tcti_receive failed: %s", Tss2_RC_Decode(rval));
            exit(1);
        }

        /*
         * The response buffer, rbuf, all fields are in big-endian, and we save
         * in big-endian.
         */
        result = write_response_to_file(ctx.output, rbuf);
        if (!result) {
        	fprintf(stderr, "Failed writing response to output file.");
            exit(1);
        }

        free(ctx.command);
        ctx.command = NULL;
    }
}

static void tpm2_tool_onexit(void) {

    close_file(ctx.input);
    close_file(ctx.output);

    free(ctx.command);
}

int main(int argc, char *argv[]) {

    setvbuf (stdin, NULL, _IONBF, 0);
    setvbuf (stdout, NULL, _IONBF, 0);
    setvbuf (stderr, NULL, _IONBF, 0);

	atexit(tpm2_tool_onexit);

	tpm2_tool_onstart(argc, argv);

	tpm2_tool_onrun(ctx.tcti);

}

