package com.zlm.download.thread;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import com.zlm.download.manage.DownloadThreadManage.IDownloadThreadCallBack;
import com.zlm.download.model.DownloadTask;

/**
 * 下载
 * 
 * @author zhangliangming
 * 
 */
public class DownloadThread extends Thread {
	/**
	 * 下载任务
	 */
	private DownloadTask task;
	/**
	 * 线程id
	 */
	private int threadID;
	/**
	 * 开始位置
	 */
	private int startIndex;
	/**
	 * 结束位置
	 */
	private int endIndex;
	/**
	 * 是否已經完成任务
	 */
	private boolean isFinish = false;
	/**
	 * 取消下载任务
	 */
	private boolean isCancel = false;
	/**
	 * 暂停任务
	 */
	private boolean isPause = false;
	/**
	 * 下载失败
	 */
	private boolean isError = false;
	/**
	 * 下载回调
	 */
	private IDownloadThreadCallBack downloadThreadCallBack;
	/**
	 * 下载进度
	 */
	private int downloadedSize = 0;
	/**
	 * 第一个线程回调
	 */
	private DTEventCallBack dteCallBack;
	/**
	 * 线程总数
	 */
	private int threadCount = 0;

	public DownloadThread(DownloadTask task, int threadID, int startIndex,
			int endIndex, int threadCount,
			IDownloadThreadCallBack downloadThreadCallBack) {
		this.threadID = threadID;
		this.startIndex = startIndex
				+ downloadThreadCallBack.getThreadDownloadedSize(task,
						threadID, threadCount);
		this.endIndex = endIndex;
		this.task = task;
		this.downloadThreadCallBack = downloadThreadCallBack;
		this.threadCount = threadCount;
	}

	public void run() {
		HttpURLConnection connection = null;
		InputStream is = null;
		RandomAccessFile randomAccessFile = null;
		try {
			connection = getHttpURLConnection(task.getDownloadUrl());
			// 设置范围，格式为Range：bytes x-y;
			connection.setRequestProperty("Range", "bytes=" + startIndex + "-"
					+ endIndex);
			connection.connect();

			// /**
			// * 代表服务器已经成功处理了部分GET请求
			// */
			// if (connection.getResponseCode() == 206) {
			randomAccessFile = new RandomAccessFile(
					new File(task.getFilePath()), "rwd");
			// 将要下载的文件写到保存在保存路径下的文件中
			is = connection.getInputStream();
			// is.skip(startIndex);
			randomAccessFile.seek(startIndex);
			byte[] buffer = new byte[1024];
			int length = -1;
			while (!isCancel && !isError && (length = is.read(buffer)) != -1
					&& (startIndex + downloadedSize) < endIndex) {

				// if (threadID == 1) {
				// System.out.println(length);
				// }
				randomAccessFile.write(buffer, 0, length);
				downloadedSize += length;
				// if (threadID == 2) {
				// System.out.println("线程==========================："
				// + threadID + "下载进度 " + downloadedSize);
				// }

				// if (downloadedSize < endIndex) {
				if ((startIndex + downloadedSize) > endIndex) {
					// 设置下载进度为完成100%
					downloadedSize = (endIndex - startIndex);
				}
				// 正在下载
				if (downloadThreadCallBack != null) {

					task.setType(DownloadTask.DOWNLOAD_DOWNLOING);

					downloadThreadCallBack.threadDownloading(task, threadID,
							threadCount, downloadedSize);
					downloadThreadCallBack.downloading();
				}
				// }
				if (isPause) {
					// 暂停任务
					if (downloadThreadCallBack != null) {
						downloadThreadCallBack.pauseed();
					}
					return;
				}
				if (isCancel) {
					if (downloadThreadCallBack != null) {
						downloadThreadCallBack.canceled();
					}
					// 取消下载任务
					return;
				}
			}
			if (!isPause && !isCancel) {
				// 完成任务
				isFinish = true;
				if (downloadThreadCallBack != null) {
					downloadThreadCallBack.finished();
				}
			}
			if (dteCallBack != null) {
				dteCallBack.notifyOtherThread();
			}
			System.out.println("线程：" + threadID + "下载完毕 " + downloadedSize);
			// } else {
			// // 服务器异常
			// isError = true;
			// }
			// }
		} catch (IOException e) {
			e.printStackTrace();

			// 下载出错
			isError = true;
			if (downloadThreadCallBack != null && task != null) {
				task.setStatus(DownloadTask.DOWNLOAD_ERROR_OTHER);
				downloadThreadCallBack.error(task);
			}

		} finally {
			try {
				if (is != null) {
					is.close();
					randomAccessFile.close();
					connection.disconnect();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * 获取相关的下载链接
	 */
	private HttpURLConnection getHttpURLConnection(String downloadUrl)
			throws IOException {
		HttpURLConnection conn = null;
		URL url = new URL(downloadUrl);
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty(
				"Accept",
				"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
		conn.setRequestProperty("Accept-Language", "zh-CN");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty(
				"User-Agent",
				"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setConnectTimeout(10 * 1000);
		return conn;
	}

	/**
	 * 暂停任务
	 */
	public void pauseTask() {
		isCancel = false;
		isPause = true;
		isFinish = false;
	}

	/**
	 * 取消任务
	 */
	public void cancelTask() {
		isCancel = true;
		isPause = false;
		isFinish = false;
	}

	public void errorTask() {
		isError = true;
		isCancel = false;
		isPause = false;
		isFinish = false;
	}

	/**
	 * 获取当前下载的进度
	 * 
	 * @return
	 */
	public int getDownloadedSize() {
		return downloadedSize;
	}

	public boolean isFinish() {
		return isFinish;
	}

	public void setFinish(boolean isFinish) {
		this.isFinish = isFinish;
	}

	public boolean isCancel() {
		return isCancel;
	}

	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}

	public boolean isPause() {
		return isPause;
	}

	public void setPause(boolean isPause) {
		this.isPause = isPause;
	}

	public boolean isError() {
		return isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

	public void setDteCallBack(DTEventCallBack dteCallBack) {
		this.dteCallBack = dteCallBack;
	}

	public interface DTEventCallBack {
		public void notifyOtherThread();
	}
}
