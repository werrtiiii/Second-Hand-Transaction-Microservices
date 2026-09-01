import http from "k6/http";
import { check } from "k6";

export const options = {
  stages: [
    { duration: "30s", target: 20 },
    { duration: "1m", target: 100 },
    { duration: "2m", target: 300 },
    { duration: "2m", target: 300 },
    { duration: "30s", target: 0 },
  ],

  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<1500"],
  },
};

const baseUrl =
  __ENV.BASE_URL || "http://host.docker.internal:18080";

export default function () {
  const response = http.get(
    `${baseUrl}/api/products?page=0&size=20`
  );

  check(response, {
    "HTTP 200": (r) => r.status === 200,
    "业务成功": (r) => {
      try {
        return JSON.parse(r.body).success === true;
      } catch {
        return false;
      }
    },
  });
}