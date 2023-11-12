package utilities

import (
	"crypto"
	"crypto/ecdsa"
	"crypto/rsa"
	"encoding/base64"
	"errors"
	"fmt"
	"math/big"

	"github.com/google/go-tpm/tpm2"
)

type AttestableData struct {
	Data      *tpm2.TPMSAttest
	Raw       []byte
	Signature *tpm2.TPMTSignature
}

func (data *AttestableData) Decode(attest []byte, signature []byte) error {
	var err error

	data.Raw = attest
	data.Data, err = tpm2.Unmarshal[tpm2.TPMSAttest](attest)
	if err != nil {
		return err
	}
	data.Signature, err = tpm2.Unmarshal[tpm2.TPMTSignature](signature)
	if err != nil {
		return err
	}

	return nil
}

func (data *AttestableData) IsQuote() bool {
	return data.Data.Type == tpm2.TPMSTAttestQuote
}

func (data AttestableData) VerifySignature(key crypto.PublicKey) error {

	alg := data.Signature.SigAlg

	switch alg {
	case tpm2.TPMAlgECDSA:
		vk, ok := key.(*ecdsa.PublicKey)
		if !ok {
			return fmt.Errorf("invalid public key type: %T", key)
		}

		parsedSignature, err := data.Signature.Signature.ECDSA()
		if err != nil {
			return fmt.Errorf("quote'S signature could not be parsed as ECDSA: %w", err)
		}

		hdata, err := computeHash(parsedSignature.Hash, data.Raw)
		if err != nil {
			return fmt.Errorf("unable to compute hash for input data: %w", err)
		}

		var R big.Int
		var S big.Int
		R.SetBytes(parsedSignature.SignatureR.Buffer)
		S.SetBytes(parsedSignature.SignatureS.Buffer)

		verified := ecdsa.Verify(vk, hdata, &R, &S)
		if !verified {
			return errors.New("verification failed")

		}
	case tpm2.TPMAlgRSASSA:
		vk, ok := key.(*rsa.PublicKey)
		if !ok {
			return fmt.Errorf("invalid public key type: %T", key)
		}

		parsedSignature, err := data.Signature.Signature.RSASSA()
		if err != nil {
			return fmt.Errorf("quote'S signature could not be parsed as RSASSA: %w", err)
		}

		hdata, err := computeHash(parsedSignature.Hash, data.Raw)
		if err != nil {
			return fmt.Errorf("unable to compute hash for input data: %w", err)
		}

		h, err := parsedSignature.Hash.Hash()
		if err != nil {
			return fmt.Errorf("unable to compute hash for algorthm: %d, reason: %w", alg, err)
		}

		if err := rsa.VerifyPKCS1v15(vk, h, hdata, parsedSignature.Sig.Buffer); err != nil {
			return errors.New("verification failed")
		}

	default:
		return fmt.Errorf("unsupported signature algorithm %d", alg)
	}
	return nil
}

func computeHash(alg tpm2.TPMAlgID, data []byte) ([]byte, error) {
	h, err := alg.Hash()
	if err != nil {
		return nil, fmt.Errorf("unable to compute hash for algorthm: %d, reason: %w", alg, err)
	}

	hh := h.New()
	if _, err := hh.Write(data); err != nil {
		return nil, err
	}
	return hh.Sum(nil), nil
}

// Parses an TPM2BPublic structure into a PublicKey
func ParseTPMKey(bytes []byte) (crypto.PublicKey, error) {
	publicBytes, err := tpm2.Unmarshal[tpm2.TPM2BPublic](bytes)
	if err != nil {
		return nil, fmt.Errorf("failed to unmarshal to Public structure %w", err)
	}

	public, err := publicBytes.Contents()
	if err != nil {
		return nil, fmt.Errorf("failed to convert to TPMT_Public: %w", err)
	}

	switch public.Type {
	case tpm2.TPMAlgRSA:
		tpmRsa, err := public.Unique.RSA()
		if err != nil {
			return nil, fmt.Errorf("failed to convert to key: %w", err)
		}

		keyParams, err := public.Parameters.RSADetail()
		if err != nil {
			return nil, fmt.Errorf("failed to get key params: %w", err)
		}

		akRsa, err := tpm2.RSAPub(keyParams, tpmRsa)
		if err != nil {
			return nil, fmt.Errorf("failed to construct RSA key")
		}
		return akRsa, nil
	case tpm2.TPMAlgECC:
		tpmEcc, err := public.Unique.ECC()
		if err != nil {
			return nil, fmt.Errorf("failed to convert to key: %w", err)
		}

		keyParams, err := public.Parameters.ECCDetail()
		if err != nil {
			return nil, fmt.Errorf("failed to get key params: %w", err)
		}

		akEcc, err := tpm2.ECCPub(keyParams, tpmEcc)
		if err != nil {
			return nil, fmt.Errorf("failed to construct RSA key")
		}
		return akEcc, nil
	default:
		return nil, fmt.Errorf("unknown key algorithm: %x", public.Type)
	}
}

func (data AttestableData) Parse() (*map[string]interface{}, error) {
	if data.Data.Type != tpm2.TPMSTAttestQuote {
		return nil, fmt.Errorf("TPMSAttest is not a quote. It is %d", data.Data.Type)
	}
	quoteData, err := data.Data.Attested.Quote()
	if err != nil {
		return nil, fmt.Errorf("could not extract quote data: %w", err)
	}

	// FIXME: We assume here that we always have at least one entry
	PCRSelectionMap := make(map[string]interface{})
	PCRSelectionMap["Hash"] = quoteData.PCRSelect.PCRSelections[0].Hash
	PCRSelectionMap["PCRs"] = TPMSPCRSelectionToList(quoteData.PCRSelect.PCRSelections)

	AttestedQuoteInfoMap := make(map[string]interface{})
	AttestedQuoteInfoMap["PCRDigest"] = base64.StdEncoding.EncodeToString(quoteData.PCRDigest.Buffer)

	AttestedQuoteInfoMap["PCRSelection"] = PCRSelectionMap

	ClockInfoMap := make(map[string]interface{})
	ClockInfoMap["Clock"] = data.Data.ClockInfo.Clock
	ClockInfoMap["ResetCount"] = data.Data.ClockInfo.ResetCount
	ClockInfoMap["RestartCount"] = data.Data.ClockInfo.RestartCount
	ClockInfoMap["Safe"] = data.Data.ClockInfo.Safe

	QualifiedSignerMap := make(map[string]interface{})
	QualifiedSignerDigestMap := make(map[string]interface{})
	// FIXME: implement decoding of TPM2BName
	QualifiedSignerDigestMap["Alg"] = nil
	QualifiedSignerDigestMap["Value"] = nil
	QualifiedSignerMap["Digest"] = QualifiedSignerDigestMap

	quoteMap := make(map[string]interface{})
	quoteMap["AttestedQuoteInfo"] = AttestedQuoteInfoMap
	quoteMap["ClockInfo"] = ClockInfoMap

	quoteMap["FirmwareVersion"] = data.Data.FirmwareVersion
	quoteMap["ExtraData"] = base64.StdEncoding.EncodeToString(data.Data.ExtraData.Buffer)
	quoteMap["Magic"] = data.Data.Magic
	quoteMap["Type"] = data.Data.Type

	signatureMap := make(map[string]interface{})
	signatureMap["Alg"] = data.Signature.SigAlg
	if data.Signature.SigAlg == tpm2.TPMAlgRSASSA {
		RSAMap := make(map[string]interface{})
		RSASSASignature, err := data.Signature.Signature.RSASSA()
		if err != nil {
			return nil, fmt.Errorf("could not convert to RSASSA signature")
		}
		RSAMap["HashAlg"] = RSASSASignature.Hash
		RSAMap["Signature"] = base64.StdEncoding.EncodeToString(RSASSASignature.Sig.Buffer)

		signatureMap["RSA"] = RSAMap
	} else {
		ECCMap := make(map[string]interface{})

		// FIXME: implement parsing for ECC signatures
		ECCMap["HashAlg"] = nil
		ECCMap["Signature"] = nil

	}

	result := make(map[string]interface{})
	result["quote"] = quoteMap
	result["signature"] = signatureMap

	return &result, nil
}

func TPMSPCRSelectionToList(selections []tpm2.TPMSPCRSelection) []int {
	var pcrs []int
	for _, selected := range selections {

		for i := 0; i < len(selected.PCRSelect)*8; i++ {
			// Check if the bit at that position is set
			if int(selected.PCRSelect[i/8])&(1<<(i%8)) != 0 {
				pcrs = append(pcrs, i)
			}
		}
	}
	return pcrs
}
