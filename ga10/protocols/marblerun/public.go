package marblerun

import (
	"crypto/tls"
	"crypto/x509"
	"encoding/json"
	"encoding/pem"
	"fmt"
	"io"
	"net/http"

	"a10/structures"
)

const (
	quoteIntent     string = "marblerun/quote"
	manifestIntent         = "marblerun/manifest"
	updateLogIntent        = "marblerun/updatelogs"
	null                   = "marblerun/null"
)

func Registration() structures.Protocol {
	intents := []string{quoteIntent, updateLogIntent, manifestIntent, null}

	return structures.Protocol{"A10MARBLERUNPROTOCOL", "Protocol to generate quote from MarbleRun", Call, intents}
}

func Call(e structures.Element, p structures.Policy, s structures.Session, aps map[string]interface{}) (map[string]interface{}, map[string]interface{}, string) {
	rtn, cps, err := requestFromMarbleRun(e, p, s, aps)

	if err != nil {
		rtn["error"] = err.Error()

		return rtn, cps, structures.CLAIMERROR
	} else {
		return rtn, cps, p.Intent
	}
}

func coordinatorTLSConfig(certs []string) (*tls.Config, error) {
	var conf tls.Config
	var parsedCerts []*x509.Certificate
	for _, cert := range certs {
		block, rest := pem.Decode([]byte(cert))
		if len(rest) != 0 {
			return nil, fmt.Errorf("expected only one certificate in each entry")
		}
		parsedCert, err := x509.ParseCertificate(block.Bytes)
		if err != nil {
			return nil, err
		}
		parsedCerts = append(parsedCerts, parsedCert)
	}

	conf.InsecureSkipVerify = true
	// Custom TLS handling that only validates the against the certificate and disables DNSName validation
	conf.VerifyConnection = func(cs tls.ConnectionState) error {
		rpool := x509.NewCertPool()
		rpool.AddCert(parsedCerts[1])
		ipool := x509.NewCertPool()
		ipool.AddCert(parsedCerts[0])
		opts := x509.VerifyOptions{
			DNSName:       "",
			Intermediates: ipool,
			Roots:         rpool,
		}
		for _, cert := range cs.PeerCertificates[1:] {
			opts.Intermediates.AddCert(cert)
		}
		_, err := cs.PeerCertificates[0].Verify(opts)
		return err
	}

	return &conf, nil
}

func requestFromMarbleRun(e structures.Element, p structures.Policy, s structures.Session, cps map[string]interface{}) (map[string]interface{}, map[string]interface{}, error) {
	var empty map[string]interface{} = make(map[string]interface{}) // this is an  *instantiated* empty map used for error situations
	var bodymap map[string]interface{}                              // this is used to store the result of the final unmarshalling  of the body received from the TA

	if p.Intent == null {
		return empty, cps, nil
	}

	suffix := ""
	if p.Intent == quoteIntent {
		suffix = "quote"
	} else if p.Intent == updateLogIntent {
		suffix = "update"
	} else if p.Intent == manifestIntent {
		suffix = "manifest"
	} else {
		return empty, nil, fmt.Errorf("intent not supported %s", p.Intent)
	}

	url := e.Endpoint + "/" + suffix
	var tr http.Transport
	// If we don't have setup any certs and are doing a quote ignore the TLS config for now
	if p.Intent == quoteIntent && len(e.MRCoordinator.Certs) == 0 {
		tr = http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		}
	} else if len(e.MRCoordinator.Certs) == 0 {
		return empty, nil, fmt.Errorf("TLS certificates for communication are required but not available")
	} else {
		conf, err := coordinatorTLSConfig(e.MRCoordinator.Certs)
		if err != nil {
			return empty, nil, fmt.Errorf("TLS certificates for communication are required but cannot be parsed")
		}
		tr = http.Transport{
			TLSClientConfig: conf,
		}
	}

	client := &http.Client{Transport: &tr}
	resp, err := client.Get(url)
	if err != nil {
		return empty, nil, err
	}
	defer resp.Body.Close()
	quoteResponse, _ := io.ReadAll(resp.Body)
	err = json.Unmarshal(quoteResponse, &bodymap)

	if err != nil {
		return empty, nil, fmt.Errorf("JSON Unmarshalling reponse from TA: %w", err)
	}

	if resp.Status != "200 OK" {
		return bodymap, nil, fmt.Errorf("MarbleRun reports error %v with response %v", resp.Status, quoteResponse)
	}

	return bodymap, cps, nil
}
