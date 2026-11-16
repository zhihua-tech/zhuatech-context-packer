/* 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.contextpacker;

import static cn.zhuatech.contextpacker.Checks.*;

import java.util.*;

/**
 * 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 先锁定必选材料和回复预留，再用 0/1 背包精确选择总价值最大的材料，输出拼装文本、未入选原因及预算。
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Algorithm {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  static Map<String, Object> compute(Map<String, Object> cfg, List<Map<String, Object>> rows) {
    unique(rows);
    int budget = integer(cfg, "budget", 1, 65536),
        reserve = integer(cfg, "reserve", 0, budget - 1),
        available = budget - reserve;
    var optional = new ArrayList<Integer>();
    var chosen = new BitSet();
    int used = 0, value = 0;
    int[] costs = new int[rows.size()], values = new int[rows.size()];
    for (int i = 0; i < rows.size(); i++) {
      var r = rows.get(i);
      str(r, "text", false);
      must(r.get("required") instanceof Boolean, "required 需要布尔值");
      costs[i] = integer(r, "tokens", 1, 1000000);
      values[i] = integer(r, "value", 0, 1000000);
      if ((Boolean) r.get("required")) {
        chosen.set(i);
        used += costs[i];
        value += values[i];
      } else optional.add(i);
    }
    must(used <= available, "必选材料超过可用预算");
    int free = available - used;
    int[] dp = new int[free + 1];
    var trace = new ArrayList<BitSet>();
    for (int i : optional) {
      var take = new BitSet();
      for (int c = free; c >= costs[i]; c--) {
        int alt = dp[c - costs[i]] + values[i];
        if (alt > dp[c]) {
          dp[c] = alt;
          take.set(c);
        }
      }
      trace.add(take);
    }
    int requiredTokens = used;
    int c = free;
    // Reconstruct from the smallest capacity achieving the same optimal value.
    while (c > 0 && dp[c - 1] == dp[free]) c--;
    for (int j = optional.size() - 1; j >= 0; j--) {
      int i = optional.get(j);
      if (trace.get(j).get(c)) {
        chosen.set(i);
        used += costs[i];
        c -= costs[i];
      }
    }
    var out = new ArrayList<>();
    var text = new StringJoiner("\n\n");
    for (int i = 0; i < rows.size(); i++) {
      var r = rows.get(i);
      if (chosen.get(i)) text.add((String) r.get("text"));
      out.add(
          Map.of(
              "id",
              r.get("id"),
              "tokens",
              costs[i],
              "selected",
              chosen.get(i),
              "reason",
              (Boolean) r.get("required")
                  ? "REQUIRED"
                  : chosen.get(i) ? "OPTIMAL" : "NOT_SELECTED"));
    }
    return Map.of(
        "metrics",
        Map.of(
            "used",
            used,
            "free",
            available - used,
            "requiredTokens", requiredTokens,
            "optionalTokens", used - requiredTokens,
            "reserved",
            reserve,
            "totalValue",
            value + dp[free]),
        "rows",
        out,
        "context",
        text.toString());
  }
}
