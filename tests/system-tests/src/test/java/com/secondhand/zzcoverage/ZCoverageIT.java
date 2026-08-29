package com.secondhand.zzcoverage;
import com.secondhand.migration.Suite;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ZCoverageIT {
 @Test void everyPublicRouteIsOwnedAndHasSuccessfulHttpEvidence()throws Exception{
  // 不把路由存在、401 或参数错误算成成功业务覆盖。
  Suite suite=Suite.INSTANCE;suite.saveEvidence();List<String> missing=new ArrayList<>();
  for(var route:suite.catalog){
   String owner=route.path("target_service").asText().replace("-service","");
   String expected=route.path("path").asText().replaceAll("[{][^}]+[}]","{}");String method=route.path("http_method").asText();
   var mappings=suite.contexts.get(owner).getBean("requestMappingHandlerMapping",RequestMappingHandlerMapping.class).getHandlerMethods().keySet();
   assertTrue(mappings.stream().anyMatch(m->m.getPatternValues().stream().anyMatch(p->p.replaceAll("[{][^}]+[}]","{}").equals(expected))&&m.getMethodsCondition().getMethods().stream().anyMatch(v->v.name().equals(method))),route.toString());
   if(!suite.covered.contains(route.path("id").asText()))missing.add(route.path("id").asText()+" "+method+" "+expected);
  }
  assertEquals(List.of(),missing,"缺少成功 HTTP 用例的公开接口");assertEquals(104,suite.covered.size());
 }
}
