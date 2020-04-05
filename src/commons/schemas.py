from rest_framework.schemas.openapi import AutoSchema

from commons.utilities import to_camel_case


class CamelCaseSchema(AutoSchema):
    def get_components(self, path, method):
        components = super().get_components(path, method)
        camelize_components = {}
        for component, content in components.items():
            camelize_content = {}
            properties = {
                to_camel_case(key): value
                for key, value in content.get("properties", {}).items()
            }
            required = [to_camel_case(item) for item in content.get("required", [])]
            content["properties"] = properties
            content["required"] = required
            camelize_components[component] = content

        return camelize_components
