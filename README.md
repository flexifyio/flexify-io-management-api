# Flexify.IO Management API

OpenAPI definitions of the [Flexify.IO](https://flexify.io/) Management Server REST API.

## User API

- [View with Redoc](https://redocly.github.io/redoc/?url=https://raw.githubusercontent.com/flexifyio/flexify-io-management-api/v2.26.0/openapi/user.yaml)
- Download: [user.yaml](https://raw.githubusercontent.com/flexifyio/flexify-io-management-api/v2.26.0/openapi/user.yaml), [user.json](https://raw.githubusercontent.com/flexifyio/flexify-io-management-api/v2.26.0/openapi/user.json)

## Partner API

- [View with Redoc](https://redocly.github.io/redoc/?url=https://raw.githubusercontent.com/flexifyio/flexify-io-management-api/v2.26.0/openapi/partner.yaml)
- Download: [partner.yaml](https://raw.githubusercontent.com/flexifyio/flexify-io-management-api/v2.26.0/openapi/partner.yaml), [partner.json](https://raw.githubusercontent.com/flexifyio/flexify-io-management-api/v2.26.0/openapi/partner.json)

## Interactive Swagger UI

To try API requests from the browser, run [Swagger UI](https://hub.docker.com/r/swaggerapi/swagger-ui) locally
with Docker:

```sh
docker run --rm -p 8080:8080 \
  -e SWAGGER_JSON_URL=https://raw.githubusercontent.com/flexifyio/flexify-io-management-api/v2.26.0/openapi/user.yaml \
  swaggerapi/swagger-ui
```

Then open <http://localhost:8080>, click **Authorize** to enter your API token, and use **Try it out** on any
operation. For the Partner API, use `partner.yaml` in the URL.

## Using the API

See the [REST API introduction](https://docs.flexify.io/348618847/rest-api/rest-api-introduction) in the
Flexify.IO documentation.

For Flexify.IO SaaS, you need an account at [manage.flexify.io](https://manage.flexify.io/). The API can also
be used with self-hosted Flexify.IO.

Send your API token with every request:

```
Authorization: Bearer <token>
```

To use the API in Postman or another tool, import one of the download links above.

## Examples

Python:

1. [List storage accounts](examples/python/list_storage_accounts.py) to get their IDs
2. [Run a migration](examples/python/run_migration.py) between two of them and monitor it

Java:

1. [List storage accounts](examples/java/ListStorageAccounts.java) to get their IDs
2. [Run a migration](examples/java/RunMigration.java) between two of them and monitor it

Questions: <support@flexify.io>
