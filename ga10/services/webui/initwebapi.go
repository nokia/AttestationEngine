package webui

import (
	"embed"
	"fmt"
	"html/template"
	"io"

	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"

	"a10/configuration"
	"a10/logging"
)

// file embedding
//
//go:embed templates/*.html
var WPFS embed.FS

const PREFIX = ""
const T = "templates/"

type TemplateRegistry struct {
	templates map[string]*template.Template
}

func (t *TemplateRegistry) Render(w io.Writer, name string, data interface{}, c echo.Context) error {
	tmpl, ok := t.templates[name]
	if !ok {
		return fmt.Errorf("Error rendering %v with %v\n", name, data)
	}

	return tmpl.ExecuteTemplate(w, "base.html", data)
}

func StartWebUI() {

	// Parse the templates
	templates := make(map[string]*template.Template)

	// It is done this way so we can have different templates for each operation...a bit ugly, but html/template is not jinja
	//dev.to/ykyuen/setup-nested-html-template-in-go-echo-web-framework-d9b

	functions := template.FuncMap{"defaultMessage": DefaultMessage, "epochToUTC": EpochToUTC, "base64decode": Base64decode, "encodeAsHexString": EncodeAsHexString, "tcgAlg": TCGAlg}

	templates["home.html"] = template.Must(template.ParseFS(WPFS, T+"home.html", T+"base.html"))
	templates["help.html"] = template.Must(template.ParseFS(WPFS, T+"help.html", T+"base.html"))
	templates["about.html"] = template.Must(template.ParseFS(WPFS, T+"about.html", T+"base.html"))

	templates["elements.html"] = template.Must(template.ParseFS(WPFS, T+"elements.html", T+"elementsummarylist.html", T+"base.html"))
	templates["policies.html"] = template.Must(template.ParseFS(WPFS, T+"policies.html", T+"policysummarylist.html", T+"base.html"))
	templates["evs.html"] = template.Must(template.ParseFS(WPFS, T+"evs.html", T+"evsummarylist.html", T+"base.html"))

	templates["element.html"] = template.Must(template.New("element.html").Funcs(functions).ParseFS(WPFS, T+"element.html", T+"base.html",
		T+"uefi.html",
		T+"txt.html",
		T+"ima.html",
		T+"tpm2.html",
		T+"tpm2key.html",
		T+"hostinformation.html",
		T+"resultvalue.html"))

	templates["policy.html"] = template.Must(template.ParseFS(WPFS, T+"policy.html", T+"base.html",
		T+"genericList.html"))
	templates["ev.html"] = template.Must(template.ParseFS(WPFS, T+"ev.html", T+"base.html",
		T+"genericList.html"))

	templates["claims.html"] = template.Must(template.New("claims.html").Funcs(functions).ParseFS(WPFS, T+"claims.html", T+"base.html"))

	templates["claim.html"] = template.Must(template.New("claim.html").Funcs(functions).ParseFS(WPFS, T+"claim.html", T+"base.html",
		T+"claim_ERROR.html",
		T+"claim_ima.html",
		T+"claim_quote.html",
		T+"claim_tpm2pcrs.html",
		T+"genericList.html"))

	templates["results.html"] = template.Must(template.New("results.html").Funcs(functions).ParseFS(WPFS, T+"results.html", T+"resultvalue.html", T+"base.html"))
	templates["result.html"] = template.Must(template.New("result.html").Funcs(functions).ParseFS(WPFS, T+"result.html", T+"resultvalue.html", T+"base.html"))

	templates["sessions.html"] = template.Must(template.New("sessions.html").Funcs(functions).ParseFS(WPFS, T+"sessions.html", T+"base.html"))
	templates["session.html"] = template.Must(template.New("session.html").Funcs(functions).ParseFS(WPFS, T+"session.html", T+"resultvalue.html", T+"base.html"))

	templates["attest.html"] = template.Must(template.New("attest.html").Funcs(functions).ParseFS(WPFS, T+"attest.html", T+"base.html"))

	templates["protocols.html"] = template.Must(template.ParseFS(WPFS, T+"protocols.html", T+"base.html"))
	templates["rules.html"] = template.Must(template.ParseFS(WPFS, T+"rules.html", T+"base.html"))

	templates["log.html"] = template.Must(template.New("log.html").Funcs(functions).ParseFS(WPFS, T+"log.html",
		T+"base.html"))

	templates["opaqueobjects.html"] = template.Must(template.ParseFS(WPFS, T+"opaqueobjects.html", T+"base.html"))
	templates["opaqueobject.html"] = template.Must(template.ParseFS(WPFS, T+"opaqueobject.html", T+"base.html"))

	templates["editelement.html"] = template.Must(template.New("editelement.html").Funcs(functions).ParseFS(WPFS, T+"editelement.html", T+"base.html"))
	templates["editpolicy.html"] = template.Must(template.New("editpolicy.html").Funcs(functions).ParseFS(WPFS, T+"editpolicy.html", T+"base.html"))
	templates["editexpectedvalue.html"] = template.Must(template.ParseFS(WPFS, T+"editexpectedvalue.html", T+"base.html"))

	// Create the router
	router := echo.New()

	router.HideBanner = true
	router.Renderer = &TemplateRegistry{
		templates: templates,
	}

	//not necessary, but I will keep this here because this is now my example of how to use middlewares
	//in echo, plus the import declaration above
	router.Use(middleware.Logger())
	router.Use(middleware.GzipWithConfig(middleware.GzipConfig{Level: 5}))

	//setup endpoints
	setupHomeEndpoints(router)
	setUpDisplayEndpoints(router)
	setUpAttestationEndpoints(router)
	setupEditEndpoints(router)

	//get configuration data
	port := ":" + configuration.ConfigData.Web.Port
	crt := configuration.ConfigData.Web.Crt
	key := configuration.ConfigData.Web.Key
	usehttp := configuration.ConfigData.Web.UseHTTP

	//start the server
	if usehttp == true {
		logging.MakeLogEntry("SYS", "startup", configuration.ConfigData.System.Name, "WEBUI", "WEB UI HTTP mode starting.")
		router.Logger.Fatal(router.Start(port))
	} else {
		logging.MakeLogEntry("SYS", "startup", configuration.ConfigData.System.Name, "WEBUI", "WEB UI HTTPS mode starting.")
		router.Logger.Fatal(router.StartTLS(port, crt, key))
	}
}

func setupEditEndpoints(router *echo.Echo) {
	router.GET(PREFIX+"/new/element", newElement)
	router.POST(PREFIX+"/new/element", processNewElement)

	router.GET(PREFIX+"/new/policy", newPolicy)
	router.POST(PREFIX+"/new/policy", processNewPolicy)

	router.GET(PREFIX+"/new/expectedvalue", newExpectedValue)

}

func setupHomeEndpoints(router *echo.Echo) {
	router.GET(PREFIX+"/", homepage)
	router.GET(PREFIX+"/help", helppage)
	router.GET(PREFIX+"/about", aboutpage)
}

func setUpDisplayEndpoints(router *echo.Echo) {
	router.GET(PREFIX+"/elements", showElements)
	router.GET(PREFIX+"/element/:itemid", showElement)
	router.GET(PREFIX+"/policies", showPolicies)
	router.GET(PREFIX+"/policy/:itemid", showPolicy)
	router.GET(PREFIX+"/expectedvalues", showExpectedValues)
	router.GET(PREFIX+"/expectedvalue/:itemid", showExpectedValue)

	router.GET(PREFIX+"/claims", showClaims)
	router.GET(PREFIX+"/claim/:itemid", showClaim)

	router.GET(PREFIX+"/results", showResults)
	router.GET(PREFIX+"/result/:itemid", showResult)

	// sessions
	router.GET(PREFIX+"/sessions", showSessions)
	router.GET(PREFIX+"/session/:itemid", showSession)

	// protocols

	router.GET(PREFIX+"/protocols", showProtocols)

	// opaqueobjects
	router.GET(PREFIX+"/opaqueobjects", showOpaqueObjects)
	router.GET(PREFIX+"/opaqueobject/:name", showOpaqueObject)

	// rules
	router.GET(PREFIX+"/rules", showRules)

	//log
	router.GET(PREFIX+"/log", showLog)
	router.GET(PREFIX+"/log/since", showLogSince)

}

func setUpAttestationEndpoints(router *echo.Echo) {
	router.GET(PREFIX+"/attest", showAttest)
	router.POST(PREFIX+"/attest", processAttest)

}
