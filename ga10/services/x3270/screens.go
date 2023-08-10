package x3270

import (
	"github.com/racingmars/go3270"
)

var titlescreen = go3270.Screen{
	{Row: 2, Col: 10, Intense: false, Content: "GA10 - Primary Menu"},

	{Row: 4, Col: 2, Intense: false, Content: "Option ==>"},

	{Row: 6, Col: 2, Intense: false, Content: "1. List elements"},
	{Row: 7, Col: 2, Intense: false, Content: "2. List sessions"},
	{Row: 8, Col: 2, Intense: false, Content: "3. Attest"},

	{Row: 4, Col: 14, Highlighting: go3270.Underscore, Write: true, Name: "option"},
	{Row: 4, Col: 18 },

}
