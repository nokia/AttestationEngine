tpm2_send: tpm2_send.c
	@gcc -o tpm2_send tpm2_send.c ${CFLAGS} -ltss2-rc -ltss2-tctildr -ldl
clean:
	@echo "Cleaning"
	@rm -f tpm2_send tpm2-send.tar.xz
dist:
	tar -cavf tpm2-send.tar.xz Makefile tpm2_send.c
