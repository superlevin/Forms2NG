# Forms2NG

**Forms2NG** lets you analyse Oracle Forms / D2k module files (`FMB`, `MMB`, `OLB`) in a web browser without Oracle Forms Builder.

## Stack (v2)

| Layer | Technology |
|-------|-----------|
| Frontend | Angular 18, TypeScript, Bootstrap 5 |
| Backend  | Spring Boot 3.3, Java 17, Spring MVC |
| XML parsing | Jakarta JAXB (Oracle Forms XML schema) |
| Build | npm / Angular CLI 18, Maven 3.x |

## Projects

- **Forms2NgWeb** – Angular 18 single-page application
- **Forms2NgWebService** – Spring Boot 3 REST API (executable JAR)

## Supported D2k Formats

Oracle Forms XML files are produced by the `frmf2xml` / `f2xml` utility included with Oracle Forms.

| Format | File suffix | Description | API prefix |
|--------|------------|-------------|------------|
| FMB    | `*_fmb.xml` | Form Module | `/rest/forms/`, `/rest/menu/` |
| MMB    | `*_mmb.xml` | Menu Module | `/rest/d2k/mmb/`, `/rest/menu/mmb` |
| OLB    | `*_olb.xml` | Object Library | `/rest/d2k/olb/`, `/rest/menu/olb` |
| PLL    | referenced inside FMB/MMB | PL/SQL Library (referenced only) | — |

## Backend Setup

### Prerequisites
- Java 17+
- Maven 3.8+

### Configuration (`application.properties`)

| Property | Default | Description |
|----------|---------|-------------|
| `forms2ng.path` | `/opt/forms2ng/` | Directory containing `*_fmb.xml`, `*_mmb.xml`, `*_olb.xml` files |
| `forms2ng.cors.allowed-origins` | `*` | Allowed CORS origins (e.g. `http://localhost:4200`) |
| `server.port` | `8080` | HTTP port |
| `server.servlet.context-path` | `/forms2ng` | Context path |

Override any property with environment variables or a custom `application.properties`:
```bash
export FORMS2NG_PATH=/data/forms/xml/
export SERVER_PORT=9090
java -jar forms2ng-service-2.0.0.jar
```

### Build & Run
```bash
cd Forms2NgWebService
mvn clean package -DskipTests
java -jar target/forms2ng-service-2.0.0.jar --forms2ng.path=/path/to/xml/files/
```

### REST API

| Method | Path | Description |
|--------|------|-------------|
| GET | `/rest/menu/` | List FMB modules |
| GET | `/rest/menu/mmb` | List MMB modules |
| GET | `/rest/menu/olb` | List OLB modules |
| GET | `/rest/forms/stats/{name}` | Form statistics (blocks, items, triggers, canvases) |
| GET | `/rest/forms/blocks/{name}` | Blocks list |
| GET | `/rest/forms/canvas/{name}` | Canvases list |
| GET | `/rest/forms/triggersforblock/{name}/{block}` | Triggers for a block |
| GET | `/rest/blocks/{name}/{block}` | Single block detail |
| GET | `/rest/d2k/mmb/{name}` | Full MMB module |
| GET | `/rest/d2k/olb/{name}` | Full OLB module |

Error responses use standard HTTP status codes:
- `404 Not Found` – file not found in configured path
- `500 Internal Server Error` – XML parse error

## Frontend Setup

### Prerequisites
- Node.js 20 LTS+
- npm 10+

### Configuration
Edit `src/environments/environment.ts` (dev) or `src/environments/environment.prod.ts` (prod):
```typescript
export const environment = {
  production: false,
  apiBase: 'http://localhost:8080/forms2ng'
};
```

### Development server
```bash
cd Forms2NgWeb
npm install
npm start           # serves at http://localhost:4200
```

### Production build
```bash
npm run build:prod  # output in dist/forms2ng-web/
```

### Tests
```bash
npm test            # runs Karma + Jasmine in ChromeHeadless
```

## Preparing XML Files

Convert Oracle Forms binary files to XML using Oracle's `frmf2xml` utility:
```bash
# Convert FMB (form)
frmf2xml MYFORM.fmb
# produces MYFORM_fmb.xml

# Convert MMB (menu module)
frmf2xml MYMENU.mmb
# produces MYMENU_mmb.xml

# Convert OLB (object library)
frmf2xml MYLIB.olb
# produces MYLIB_olb.xml
```

Place all `*_fmb.xml`, `*_mmb.xml`, `*_olb.xml` files in the directory configured by `forms2ng.path`.

## Legacy Frontend

The original AngularJS 1.2 frontend is preserved in `Forms2NgWeb/app-legacy/` for reference.
