package vn.hoangminh.erp.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.CorsFilter;

class CorsConfigurationTest {
  @Test
  void loginPreflightAllowsConfiguredFrontendAndRejectsOtherOrigins() throws Exception {
    var environment = new StandardEnvironment();
    var properties = new YamlPropertySourceLoader()
        .load("application", new ClassPathResource("application.yml")).getFirst();
    String origins = environment.resolvePlaceholders(
        (String) properties.getProperty("erp.cors.allowed-origins"));
    var source = new SecurityConfig().corsConfigurationSource(
        new AppProperties(null, null, new AppProperties.Cors(origins)));
    var filter = new CorsFilter(source);

    var allowed = preflight(filter, "https://frontendhoangminh.vercel.app");
    assertThat(allowed.getStatus()).isEqualTo(200);
    assertThat(allowed.getHeader("Access-Control-Allow-Origin"))
        .isEqualTo("https://frontendhoangminh.vercel.app");
    assertThat(allowed.getHeader("Access-Control-Allow-Methods")).contains("POST");
    assertThat(allowed.getHeader("Access-Control-Allow-Headers"))
        .containsIgnoringCase("content-type");

    var rejected = preflight(filter, "https://untrusted.example");
    assertThat(rejected.getStatus()).isEqualTo(403);
    assertThat(rejected.getHeader("Access-Control-Allow-Origin")).isNull();
  }

  private MockHttpServletResponse preflight(CorsFilter filter, String origin) throws Exception {
    var request = new MockHttpServletRequest("OPTIONS", "/api/auth/dang-nhap");
    request.setServletPath("/api/auth/dang-nhap");
    request.addHeader("Origin", origin);
    request.addHeader("Access-Control-Request-Method", "POST");
    request.addHeader("Access-Control-Request-Headers", "content-type");
    var response = new MockHttpServletResponse();
    filter.doFilter(request, response, (req, res) -> {
      throw new AssertionError("Preflight must be handled before authentication");
    });
    return response;
  }
}
