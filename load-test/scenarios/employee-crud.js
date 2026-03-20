import http from "k6/http";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const EMPLOYEES_URL = `${BASE_URL}/employees`;

export default function () {
  const payload = JSON.stringify({
    firstName: `Load-${__VU}-${__ITER}`,
    lastName: `Test-${__VU}-${__ITER}`,
    employeeType: "WORKER",
  });

  const headers = { "Content-Type": "application/json" };
  const byIdTag = { tags: { name: "GET /employees/{uuid}" } };

  const createResponse = http.post(EMPLOYEES_URL, payload, { headers });
  const employeeUuid = createResponse.json("uuid");

  http.get(`${EMPLOYEES_URL}/${employeeUuid}`, byIdTag);

  http.get(EMPLOYEES_URL);

  const updatePayload = JSON.stringify({
    firstName: `Updated-${__VU}-${__ITER}`,
    lastName: `Employee-${__VU}-${__ITER}`,
    employeeType: "MANAGER",
  });

  http.put(`${EMPLOYEES_URL}/${employeeUuid}`, updatePayload, {
    headers,
    tags: { name: "PUT /employees/{uuid}" },
  });

  const patchPayload = JSON.stringify({
    firstName: `Patched-${__VU}-${__ITER}`,
  });

  http.patch(`${EMPLOYEES_URL}/${employeeUuid}`, patchPayload, {
    headers,
    tags: { name: "PATCH /employees/{uuid}" },
  });

  http.del(`${EMPLOYEES_URL}/${employeeUuid}`, null, {
    tags: { name: "DELETE /employees/{uuid}" },
  });
}
