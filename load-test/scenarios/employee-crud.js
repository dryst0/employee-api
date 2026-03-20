import http from "k6/http";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

export default function () {
  const payload = JSON.stringify({
    firstName: `Load-${__VU}-${__ITER}`,
    lastName: `Test-${__VU}-${__ITER}`,
    employeeType: "WORKER",
  });

  const headers = { "Content-Type": "application/json" };

  const createResponse = http.post(`${BASE_URL}/employees`, payload, {
    headers,
  });
  const employeeUuid = createResponse.json("uuid");

  http.get(`${BASE_URL}/employees/${employeeUuid}`);

  http.get(`${BASE_URL}/employees`);

  http.del(`${BASE_URL}/employees/${employeeUuid}`);
}
