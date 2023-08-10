package x3270

import (
	"github.com/racingmars/go3270"
)

var titlescreenrules = go3270.Rules{
	"option":    {Validator: go3270.NonBlank, MustChange: true},
	
}