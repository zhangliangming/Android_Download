package com.zlm.download.manage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

import com.zlm.download.model.DownloadTask;
import com.zlm.download.thread.DownloadThread;
import com.zlm.download.thread.DownloadThread.DTEventCallBack;
import com.zlm.download.util.DownloadThreadPool.IDownloadTaskFinishCallBack;
import com.zlm.download.util.DownloadThreadPool.IDownloadThreadEventCallBack;

/**
 * 多线程下载管理
 * 
 * @author zhangliangming
 * 
 */
public class DownloadThreadManage {
	/**
	 * 任务
	 */
	private DownloadTask task;
	/**
	 * 线程数
	 */
	private int threadCount = 1;
	/**
	 * 睡眠线程时间
	 */
	private int sleepTime = 100;
	/**
	 * 下载任务线程
	 */
	private DownloadThread[] downloadThreads;
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
	 * 正在下载
	 */
	private boolean isDownloading = false;
	/**
	 * 任务完成事件回调
	 */
	private IDownloadTaskFinishCallBack finishCallBack;
	/**
	 * 下载线程事件回调
	 */
	private IDownloadThreadEventCallBack downloadThreadEventCallBack;
	/**
	 * 时间线程
	 */
	private Thread timeThread = null;

	/**
	 * 上一次下载进度
	 */
	private int olddownloadedSize = 0;

	public DownloadThreadManage(DownloadTask task, int threadCount,
			int sleepTime, IDownloadTaskFinishCallBack finishCallBack,
			IDownloadThreadEventCallBack downloadThreadEventCallBack) {
		this.olddownloadedSize = (int) task.getOldDownloadedSize();
		this.sleepTime = sleepTime;
		this.finishCallBack = finishCallBack;
		this.task = task;
		this.downloadThreadEventCallBack = downloadThreadEventCallBack;
		this.threadCount = threadCount;
	}

	public DownloadThreadManage(DownloadTask task, int threadCount,
			int sleepTime) {
		this.olddownloadedSize = (int) task.getOldDownloadedSize();
		this.sleepTime = sleepTime;
		this.task = task;
		this.threadCount = threadCount;
	}

	public boolean isFinish() {
		return isFinish;
	}

	public boolean isDownloading() {
		return isDownloading;
	}

	public boolean isCancel() {
		return isCancel;
	}

	public boolean isPause() {
		return isPause;
	}

	public boolean isError() {
		return isError;
	}

	public void setFinishCallBack(IDownloadTaskFinishCallBack finishCallBack) {
		this.finishCallBack = finishCallBack;
	}

	public void setDownloadThreadEventCallBack(
			IDownloadThreadEventCallBack downloadThreadEventCallBack) {
		this.downloadThreadEventCallBack = downloadThreadEventCallBack;
	}

	/**
	 * 下载任务事件回调
	 */
	private IDownloadThreadCallBack downloadThreadCallBack = new IDownloadThreadCallBack() {

		@Override
		public void threadDownloading(DownloadTask task, int threadID,
				int threadCount, int downloadedSize) {
			threadDownloadingTask(task, threadID, threadCount, downloadedSize);
		}

		@Override
		public void downloading() {
			downloadingTask();
		}

		@Override
		public void pauseed() {
			pauseedTask();
		}

		@Override
		public void finished() {
			finishedTask();
		}

		@Override
		public void error(DownloadTask task) {
			errorTask(task);
		}

		@Override
		public void canceled() {
			canceledTask();
		}

	};

	/**
	 * 线程下载
	 * 
	 * @param task
	 * @param threadID
	 * @param threadCount
	 * @param downloadedSize
	 */
	private synchronized void threadDownloadingTask(DownloadTask task,
			int threadID, int threadCount, int downloadedSize) {
		if (downloadThreadEventCallBack != null && task != null) {
			downloadThreadEventCallBack.threadDownloading(task, threadID,
					threadCount, downloadedSize);
		}
	}

	private synchronized void downloadingTask() {
		if (timeThread == null) {
			timeThread = new Thread(new TimeRunable());
			timeThread.start();
		}
	}

	/**
	 * 暂停
	 */
	private synchronized void pauseedTask() {

		int downloadedSize = olddownloadedSize;
		for (int i = 0; i < downloadThreads.length; i++) {
			DownloadThread downloadThread = downloadThreads[i];
			if (downloadThread != null) {
				if (!downloadThread.isPause()) {
					return;
				}
				downloadedSize += downloadThread.getDownloadedSize();
			}
		}
		if (timeThread != null) {
			timeThread = null;
		}
		if (downloadThreadEventCallBack != null && task != null) {
			isPause = true;
			isDownloading = false;
			//
			task.setType(DownloadTask.DOWNLOAD_PAUSE);
			downloadThreadEventCallBack.pauseed(task, downloadedSize);

			if (finishCallBack != null) {
				finishCallBack.notifyDownloadThread();
			}
		}

	}

	/**
	 * 下载完成
	 */
	private synchronized void finishedTask() {

		int downloadedSize = olddownloadedSize;
		for (int i = 0; i < downloadThreads.length; i++) {
			DownloadThread downloadThread = downloadThreads[i];
			if (downloadThread != null) {
				if (!downloadThread.isFinish()) {
					return;
				}
				downloadedSize += downloadThread.getDownloadedSize();
			}
		}
		if (downloadedSize >= task.getFileSize()) {
			isFinish = true;
			if (timeThread != null) {
				timeThread = null;
			}

			if (finishCallBack != null) {
				finishCallBack.notifyDownloadThread();
			}
			if (downloadThreadEventCallBack != null && task != null) {
				isDownloading = false;
				task.setType(DownloadTask.DOWNLOAD_FINISH);
				downloadThreadEventCallBack.finished(task);
			}
		}

	}

	/**
	 * 下载出错
	 * 
	 * @param task
	 */
	private synchronized void errorTask(DownloadTask task) {

		isError = true;
		for (int i = 0; i < downloadThreads.length; i++) {
			DownloadThread downloadThread = downloadThreads[i];
			if (downloadThread != null) {
				if (!downloadThread.isError()) {
					downloadThread.errorTask();
					return;
				}
			}
		}
		if (timeThread != null) {
			timeThread = null;
		}
		if (downloadThreadEventCallBack != null) {
			isDownloading = false;

			downloadThreadEventCallBack.error(task);
			if (finishCallBack != null) {
				finishCallBack.notifyDownloadThread();
			}
		}

	}

	/**
	 * 取消
	 */
	private synchronized void canceledTask() {

		for (int i = 0; i < downloadThreads.length; i++) {
			DownloadThread downloadThread = downloadThreads[i];
			if (downloadThread != null) {
				if (!downloadThread.isCancel()) {
					return;
				}
			}
		}
		if (timeThread != null) {
			timeThread = null;
		}
		if (downloadThreadEventCallBack != null && task != null) {
			isCancel = true;
			isDownloading = false;
			//
			task.setType(DownloadTask.DOWNLOAD_CANCEL);
			downloadThreadEventCallBack.canceled(task);
			if (finishCallBack != null) {
				finishCallBack.notifyDownloadThread();
			}
		}

	}

	/**
	 * 单一任务下载
	 */
	public void startSingleTask() {

		try {
			// 获取该网络资源文件的长度
			final int length = getFileLength(new URL(task.getDownloadUrl()));
			// final int length = (int) task.getFileSize();
			task.setFileSize(length);
			File destFile = new File(task.getFilePath());
			File temp = destFile.getParentFile();
			if (!temp.exists()) {
				temp.mkdirs();
			}
			if (!destFile.exists()) {
				// 目标文件不存在 ，则创建目标文件
				task.setOldDownloadedSize(0);
				destFile.createNewFile();
				RandomAccessFile accessFile = new RandomAccessFile(destFile,
						"rwd");
				accessFile.setLength(task.getFileSize());
				accessFile.close();
			}
			downloadThreads = new DownloadThread[threadCount];
			// 平均每一个线程下载的文件大小.
			final int blockSize = length / threadCount;
			// 整个下载资源整除后剩下的余数取模
			final int left = length % threadCount;
			int threadId = 1;
			if (threadId <= threadCount) {
				int startIndex = (threadId - 1) * blockSize;
				int endIndex = threadId * blockSize;
				if (threadId == threadCount) {// 最后一个线程下载的长度要稍微长一点
					// 最后一个线程下载指定endIndex+left个字节
					endIndex = endIndex + left;
				}
				// System.out.println("线程：" + threadId + "下载:---" + startIndex
				// + "--->" + endIndex);

				final DownloadThread dt = new DownloadThread(task, threadId,
						startIndex, endIndex, threadCount,
						downloadThreadCallBack);
				if (dt.isFinish()) {
					notifyOtherThreadTask(blockSize, length, left);
				}
				downloadThreads[threadId - 1] = dt;
				downloadThreads[threadId - 1].start();
				downloadThreads[threadId - 1]
						.setDteCallBack(new DTEventCallBack() {

							@Override
							public void notifyOtherThread() {
								System.out.println("触发剩下的线程");
								notifyOtherThreadTask(blockSize, length, left);
							}
						});
			}

		} catch (Exception e) {
			isError = true;
			e.printStackTrace();
		}

	}

	/**
	 * 其它线程下载任务
	 * 
	 * @param blockSize
	 * @param length
	 */
	protected void notifyOtherThreadTask(int blockSize, int length, int left) {
		for (int threadId = 2; threadId <= threadCount; threadId++) {
			// 第一个线程下载的开始位置
			int startIndex = (threadId - 1) * blockSize;
			int endIndex = threadId * blockSize;
			if (threadId == threadCount) {// 最后一个线程下载的长度要稍微长一点
				// 最后一个线程下载指定endIndex+left个字节
				endIndex = endIndex + left;
			}
			// System.out.println("线程：" + threadId + "下载:---" + startIndex
			// + "--->" + endIndex);
			DownloadThread dt = new DownloadThread(task, threadId, startIndex,
					endIndex, threadCount, downloadThreadCallBack);
			if (dt.isFinish()) {
				continue;
			}
			downloadThreads[threadId - 1] = dt;
			downloadThreads[threadId - 1].start();
		}

	}

	/**
	 * 任务下载
	 */
	public void start() {

		try {
			// 获取该网络资源文件的长度
			int length = getFileLength(new URL(task.getDownloadUrl()));
			// int length = (int) task.getFileSize();
			task.setFileSize(length);
			File destFile = new File(task.getFilePath());
			File temp = destFile.getParentFile();
			if (!temp.exists()) {
				temp.mkdirs();
			}
			if (!destFile.exists()) {
				// 目标文件不存在 ，则创建目标文件
				task.setOldDownloadedSize(0);
				destFile.createNewFile();
				RandomAccessFile accessFile = new RandomAccessFile(destFile,
						"rwd");
				accessFile.setLength(task.getFileSize());
				accessFile.close();
			}
			downloadThreads = new DownloadThread[threadCount];
			// 平均每一个线程下载的文件大小.
			int blockSize = length / threadCount;
			int left = length % threadCount;
			for (int threadId = 1; threadId <= threadCount; threadId++) {
				// 第一个线程下载的开始位置
				int startIndex = (threadId - 1) * blockSize;
				// if(threadId %2 == 0){
				// startIndex ++;
				// }
				int endIndex = threadId * blockSize;
				if (threadId == threadCount) {// 最后一个线程下载的长度要稍微长一点
					// 最后一个线程下载指定endIndex+left个字节
					endIndex = endIndex + left;
				}
				// System.out.println("线程：" + threadId + "下载:---" + startIndex
				// + "--->" + endIndex);
				DownloadThread dt = new DownloadThread(task, threadId,
						startIndex, endIndex, threadCount,
						downloadThreadCallBack);
				if (dt.isFinish()) {
					continue;
				}
				downloadThreads[threadId - 1] = dt;
				downloadThreads[threadId - 1].start();
			}

		} catch (Exception e) {
			isError = true;
			e.printStackTrace();
		}

	}

	private class TimeRunable implements Runnable {

		@Override
		public void run() {
			isDownloading = true;
			while (true) {
				try {

					if (downloadThreadEventCallBack != null && !isCancel
							&& !isError && !isPause && !isFinish
							&& isDownloading) {
						updateDownloadUI();
					}
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 更新下载进度
	 */
	private void updateDownloadUI() {
		int downloadedSize = olddownloadedSize;
		for (int i = 0; i < downloadThreads.length; i++) {
			DownloadThread downloadThread = downloadThreads[i];
			if (downloadThread != null)
				downloadedSize += downloadThread.getDownloadedSize();
		}
		if (downloadThreadEventCallBack != null && task != null) {
			downloadThreadEventCallBack.downloading(task, downloadedSize);
		}
	}

	/**
	 * 暂停
	 */
	public void pause() {
		for (int i = 0; i < downloadThreads.length; i++) {
			DownloadThread downloadThread = downloadThreads[i];
			if (downloadThread != null)
				downloadThread.pauseTask();
		}
	}

	/**
	 * 取消
	 */
	public void cancel() {
		for (int i = 0; i < downloadThreads.length; i++) {
			DownloadThread downloadThread = downloadThreads[i];
			if (downloadThread != null)
				downloadThread.cancelTask();
		}
	}

	/**
	 * 根据URL获取该URL所指向的资源文件的长度
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private int getFileLength(URL url) throws IOException {
		int length = 0;
		URLConnection urlConnection = url.openConnection();
		int size = urlConnection.getContentLength();
		length = size;
		System.out.println("下载的文件大小：" + length);
		return length;
	}

	/**
	 * 线程任务事件回调
	 * 
	 * @author zhangliangming
	 * 
	 */
	public interface IDownloadThreadCallBack {
		/**
		 * 线程的下载进度
		 * 
		 * @param task
		 * @param threadID
		 *            线程id
		 * @param threadCount
		 *            线程总数
		 * @param downloadedSize
		 *            线程下载进度
		 */
		public void threadDownloading(DownloadTask task, int threadID,
				int threadCount, int downloadedSize);

		/**
		 * 下载中回调接口
		 */
		public void downloading();

		/**
		 * 暂停回调接口
		 */
		public void pauseed();

		/**
		 * 下载完成回调接口
		 */
		public void finished();

		/**
		 * 错误回调接口
		 */
		public void error(DownloadTask task);

		/**
		 * 
		 */
		public void canceled();
	}
}
