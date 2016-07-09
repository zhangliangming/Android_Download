package com.zlm.download.manage;

import com.zlm.download.util.DownloadThreadPool;

/**
 * 下载管理
 * 
 * @author zhangliangming
 * 
 */
public class DownloadManage {

	/**
	 * 皮肤线程
	 */
	private static DownloadThreadPool skinTM;
	/**
	 * 应用线程
	 */
	private static DownloadThreadPool apkTM;
	/**
	 * 歌曲线程
	 */
	private static DownloadThreadPool songNetTM;
	/**
	 * 图片
	 */
	private static DownloadThreadPool imageTM;

	/**
	 * 获取图片线程管理
	 * 
	 * @return
	 */
	public static DownloadThreadPool getImageTM() {
		if (imageTM == null) {
			imageTM = new DownloadThreadPool();
		}
		return imageTM;
	}

	/**
	 * 获取皮肤线程管理
	 * 
	 * @return
	 */
	public static DownloadThreadPool getSkinTM() {
		if (skinTM == null) {
			skinTM = new DownloadThreadPool();
		}
		return skinTM;
	}

	/**
	 * 获取应用线程管理
	 * 
	 * @return
	 */
	public static DownloadThreadPool getAPKTM() {
		if (apkTM == null) {
			apkTM = new DownloadThreadPool();
		}
		return apkTM;
	}

	/**
	 * 获取在线歌曲线程管理
	 * 
	 * @param context
	 * @return
	 */
	public static DownloadThreadPool getSongNetTM() {
		if (songNetTM == null) {
			songNetTM = new DownloadThreadPool();
		}
		return songNetTM;
	}

}
