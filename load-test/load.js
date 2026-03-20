export { default } from "./scenarios/employee-crud.js";

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    http_req_failed: ["rate==0"],
  },
};
