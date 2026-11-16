/* 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.contextpacker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.*;
import tools.jackson.databind.ObjectMapper;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@SuppressWarnings("unchecked")
class AlgorithmTest {
  Map<String, Object> cfg;
  List<Map<String, Object>> data;

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @BeforeEach
  void load() throws Exception {
    var fixture =
        new ObjectMapper()
            .readValue(getClass().getResourceAsStream("/static/catalog.json"), Map.class);
    cfg = (Map<String, Object>) fixture.get("settings");
    data = (List<Map<String, Object>>) fixture.get("rows");
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void boundaryCases() {
    var rng = new Random(4711);
    for (int z = 0; z < 30; z++) {
      var box = new ArrayList<Map<String, Object>>();
      int[] w = new int[7], v = new int[7];
      for (int i = 0; i < 7; i++) {
        w[i] = 1 + rng.nextInt(9);
        v[i] = rng.nextInt(12);
        box.add(
            Map.of("id", "N" + i, "text", "T", "tokens", w[i], "value", v[i], "required", false));
      }
      int best = 0;
      for (int mask = 0; mask < 128; mask++) {
        int cost = 0, total = 0;
        for (int i = 0; i < 7; i++)
          if ((mask & (1 << i)) != 0) {
            cost += w[i];
            total += v[i];
          }
        if (cost <= 13) best = Math.max(best, total);
      }
      var r = Algorithm.compute(Map.of("budget", 15, "reserve", 2), box);
      assertEquals(best, ((Map<?, ?>) r.get("metrics")).get("totalValue"));
    }
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void knownResults() {
    var result = Algorithm.compute(cfg, data);
    var m = (Map<String, Object>) result.get("metrics");
    var out = (List<Map<String, Object>>) result.get("rows");
    var first = out.getFirst();
    assertEquals(13, ((Number) m.get("totalValue")).intValue());
    assertEquals(80, m.get("used"));
    assertEquals(false, out.get(1).get("selected"));
    assertEquals(true, out.get(2).get("selected"));
    assertTrue(result.get("context").toString().contains("异常复核"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsInvalidInput() {
    cfg.put("budget", 10);
    assertThrows(Model.Failure.class, () -> Algorithm.compute(cfg, data));
  }
}
