window.onload = function() {
  //<editor-fold desc="Changeable Configuration Block">

  // the following lines will be replaced by docker/configurator, when it runs in a docker-container
  window.ui = SwaggerUIBundle({
    url: "/v3/api-docs/all",
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout" ,

  "configUrl" : "/v3/api-docs/swagger-config",
  "operationsSorter" : "method",
  "persistAuthorization" : true,
  "tagsSorter" : "alpha",
  "tryItOutEnabled" : true,
  "validatorUrl" : "",
  // custom CSS for dark + green theme
  "customCssUrl" : "/swagger-ui-dark-green.css"

  });

  //</editor-fold>
};
