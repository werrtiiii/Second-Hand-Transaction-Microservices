import http from "k6/http";
import exec from "k6/execution";
import { check } from "k6";

export const options = {
  scenarios: {
    comparison: {
      executor: "constant-vus",
      vus: Number(__ENV.VUS || 50),
      duration: __ENV.DURATION || "45s",
      gracefulStop: "5s",
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<1500"],
    checks: ["rate>0.99"],
  },
};

const baseUrl = __ENV.BASE_URL || "http://host.docker.internal:18080";
const endpoints = [
  { name: "products", path: "/api/products?page=0&size=20" },
  { name: "regions", path: "/api/regions" },
  { name: "user-rating", path: "/api/users/2/rating" },
];

export default function () {
  // 全局轮询三条公开只读接口，确保每轮测试的接口分布一致。
  const endpoint = endpoints[exec.scenario.iterationInTest % endpoints.length];
  const response = http.get(baseUrl + endpoint.path, {
    tags: { endpoint: endpoint.name },
  });
  check(response, {
    "HTTP 200": (result) => result.status === 200,
    "business success": (result) => {
      try {
        return JSON.parse(result.body).success === true;
      } catch {
        return false;
      }
    },
  });
}
