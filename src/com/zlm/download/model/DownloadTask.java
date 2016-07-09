package com.zlm.download.model;

import com.zlm.download.manage.DownloadThreadManage;

/**
 * 下载任务类
 * 
 * @author zhangliangming
 * 
 */
public class DownloadTask {

	private static int i = 1000;

	public static final int SKIN = i++;// 皮肤
	public static final int APK = i++;// 应用
	public static final int SONG_DOWNLOAD = i++;// 歌曲下载
	public static final int SONG_NET = i++;// 网络在线歌曲
	public static final int PLUGINS = i++;// 插件下载
	public static final int IMAGE = i++;// 图片下载
	public static final int KSCFILE = i++;// 歌词文件下载
	public static final int OTHERFILE = i++;// 其它文件下载

	public static final int DOWNLOAD_WAITING = i++;// 等待下载
	public static final int DOWNLOAD_WAITING_CANCEL = i++;// 等待取消
	public static final int DOWNLOAD_DOWNLOING = i++;// 下载中
	public static final int DOWNLOAD_PAUSE = i++;// 下载暂停
	public static final int DOWNLOAD_CANCEL = i++;// 下载取消
	public static final int DOWNLOAD_ERROR_NONET = i++; // 下载失败-无网络
	public static final int DOWNLOAD_ERROR_NOTWIFI = i++; // 下载失败-不是wifi
	public static final int DOWNLOAD_ERROR_OTHER = i++; // 下载失败-其它原因
	public static final int DOWNLOAD_FINISH = i++;// 下载完成

	/**
	 * 任务id
	 */
	private String tid;

	/**
	 * 初始化状态
	 */
	private int status = DOWNLOAD_WAITING;
	/**
	 * 下载地址
	 */
	private String downloadUrl;
	/**
	 * 文件路径
	 */
	private String filePath;
	/**
	 * 文件大小
	 */
	private long fileSize;

	/**
	 * 下载任务类型
	 */
	private int type = APK;
	/**
	 * 添加时间
	 */
	private String addTime;
	/**
	 * 完成时间
	 */
	private String finishTime;
	/**
	 * 历史下载进度
	 */
	private int oldDownloadedSize;
	/**
	 * 多线程下载管理
	 */
	private DownloadThreadManage downloadThreadManage;

	public DownloadTask() {

	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}

	public String getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}

	public int getOldDownloadedSize() {
		return oldDownloadedSize;
	}

	public void setOldDownloadedSize(int oldDownloadedSize) {
		this.oldDownloadedSize = oldDownloadedSize;
	}

	public void setDownloadThreadManage(DownloadThreadManage downloadThreadManage) {
		this.downloadThreadManage = downloadThreadManage;
	}

	public DownloadThreadManage getDownloadThreadManage() {
		return downloadThreadManage;
	}
}
