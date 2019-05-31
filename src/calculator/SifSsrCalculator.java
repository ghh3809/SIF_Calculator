package calculator;

import java.util.ArrayList;

public class SifSsrCalculator {

	public static void main(String[] args) {
		SifSsrCalculator ssc = new SifSsrCalculator();
		ssc.getSsrExpTable(11);
	}
	
	/**
	 * 获取SSR喂卡时的数据表
	 * @param maxCards 最大计算卡数
	 */
	public void getSsrExpTable(int maxCards) {
		
		// 初始化
		int[][] exp = new int[maxCards + 1][];
		String[][] expMethod = new String[maxCards + 1][];
		for (int i = 0; i <= maxCards; i ++) {
			exp[i] = new int[500];
			expMethod[i] = new String[500];
		}
		int[] accExp = new int[] {0, 0, 2, 6, 14, 26, 50, 82, 127, Integer.MAX_VALUE}; // 升级累计经验值
		int[] pracExp = new int[] {0, 3, 6, 12, 18, 27, 36, 45, 66, Integer.MAX_VALUE}; // 练习获得经验值
		int[] accExpMap = new int[500];
		int accExpIndex = 1;
		for (int i = 0; i < 500; i ++) {
			if (accExp[accExpIndex + 1] <= i) accExpIndex ++;
			accExpMap[i] = accExpIndex;
		}
		
		// 利用动态规划方法进行计算，f(n,m)表示在拥有n张SSR卡（包括胚子卡），且投入m*100经验时，可获得的单卡经验数
		// 迭代基为：f(1, m) = m
		// 迭代方法为：f(n, m) = max(E(f(n1, m1)) + E(f(n2, m2)) + ... + f(1, mk))，且满足n1+n2+...+1=n, m1+m2+...+mk=m
		
		// 迭代基计算
		ArrayList<ArrayList<Integer>> thresh = new ArrayList<ArrayList<Integer>>();
		thresh.add(new ArrayList<Integer>());
		thresh.add(new ArrayList<Integer>());
		for (int j = 0; j < 500; j ++) {
			exp[1][j] = j;
			if ((j == 0) || (accExpMap[exp[1][j]] > accExpMap[exp[1][j-1]])) thresh.get(1).add(j);
			if (accExpMap[exp[1][j]] >= 8) break;
		}
		
		// 迭代计算
		for (int i = 2; i <= maxCards; i ++) {
			thresh.add(new ArrayList<Integer>());
			int foodCards = i - 1;
			ArrayList<ArrayList<Integer>> segments = getDist(foodCards, foodCards);
			for (int j = 0; j <= 500; j ++) {
				for (ArrayList<Integer> segment : segments) {
					int parts = segment.size();
					int[] threshIndex = new int[parts];
					while (threshIndex[0] != -1) {
						int sumExp = getSumExp(threshIndex, segment, thresh, exp, accExpMap, pracExp);
						int usedExp = getUsedExp(threshIndex, segment, thresh);
						sumExp += j - usedExp;
						if (sumExp > exp[i][j]) {
							exp[i][j] = sumExp;
							StringBuilder sb = new StringBuilder();
							sb.append("[");
							for (int k = 0; k < threshIndex.length; k ++) {
								sb.append(segment.get(k) + "卡+" + thresh.get(segment.get(k)).get(threshIndex[k]) * 100 + "经验, ");
							}
							sb.append("补" + (j - usedExp) * 100 + "经验]");
							expMethod[i][j] = sb.toString();
						}
						refreshThreshIndex(threshIndex, j, thresh, segment);
					}
				}
				System.out.println("卡片数 = " + i + ", 投入经验数 = " + j * 100 + ", 产出经验数 = " + exp[i][j] * 100 + ", Lv = " + accExpMap[exp[i][j]]
						+ ", 喂卡路径：" + expMethod[i][j]);
				if ((j == 0) || (accExpMap[exp[i][j]] > accExpMap[exp[i][j-1]])) thresh.get(i).add(j);
				if (accExpMap[exp[i][j]] >= 8) break;
			}
		}
		
	}
	
	/**
	 * 特定num可以进行的划分方法
	 * @param num
	 * @return
	 */
	private ArrayList<ArrayList<Integer>> getDist(int num, int maxSeg) {
		ArrayList<ArrayList<Integer>> res = new ArrayList<ArrayList<Integer>>();
		if (num == 0) {
			res.add(new ArrayList<Integer>());
			return res;
		}
		for (int i = maxSeg; i > 0; i --) {
			if (num < i) continue;
			ArrayList<ArrayList<Integer>> partRes = getDist(num - i, Math.min(maxSeg, i));
			for (ArrayList<Integer> part : partRes) {
				part.add(i);
				res.add(part);
			}
		}
		return res;
	}
	
	/**
	 * 刷新使用档位
	 * @param threshIndex
	 * @param totalExp
	 * @param thresh
	 * @param segment
	 */
	private void refreshThreshIndex(int[] threshIndex, int totalExp, ArrayList<ArrayList<Integer>> thresh, ArrayList<Integer> segment) {
		threshIndex[0] ++;
		int usedExp = getUsedExp(threshIndex, segment, thresh);
		int overIndex = 0;
		while (usedExp > totalExp) {
			threshIndex[overIndex] = 0;
			overIndex ++;
			if (overIndex >= threshIndex.length) {
				threshIndex[0] = -1;
				break;
			}
			threshIndex[overIndex] ++;
			usedExp = getUsedExp(threshIndex, segment, thresh);
		}
	}
	
	/**
	 * 获取已消耗经验
	 * @param threshIndex
	 * @param segment
	 * @param thresh
	 * @return
	 */
	private int getUsedExp(int[] threshIndex, ArrayList<Integer> segment, ArrayList<ArrayList<Integer>> thresh) {
		int usedExp = 0;
		for (int k = 0; k < threshIndex.length; k ++) {
			usedExp += thresh.get(segment.get(k)).get(threshIndex[k]);
		}
		return usedExp;
	}
	
	/**
	 * 获取总技能经验
	 * @param threshIndex
	 * @param segment
	 * @param thresh
	 * @param exp
	 * @return
	 */
	private int getSumExp(int[] threshIndex, ArrayList<Integer> segment, ArrayList<ArrayList<Integer>> thresh, int[][] exp, int[] accExpMap, int[] pracExp) {
		int sumExp = 0;
		for (int k = 0; k < threshIndex.length; k ++) {
			sumExp += pracExp[accExpMap[exp[segment.get(k)][thresh.get(segment.get(k)).get(threshIndex[k])]]];
		}
		return sumExp;
	}
	
}
