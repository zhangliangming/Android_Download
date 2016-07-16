package com.zlm.download.util;

import java.util.ArrayList;

import com.zlm.download.manage.DownloadThreadManage;
import com.zlm.download.model.DownloadTask;

/**
 * 下载线程尺池，目前只实现单任务多线程下载
 * 
 * @author zhangliangming
 * 
 */
public class DownloadThreadPool {
	/**
	 * 任务列表
	 */
	private ArrayList<DownloadTask> tasks = new ArrayList<DownloadTask>();
	/**
	 * 下载线程
	 */
	private Thread downloadThread;
	/**
	 * 是否是线程等待
	 */
	private boolean isWaiting = false;
	/**
	 * 下载线程事件回调
	 */
	private IDownloadThreadEventCallBack downloadThreadEventCallBack = null;
	/**
	 * 正在下载的任务id
	 */
	private String isDownloadingTid = "";

	/**
	 * 任务完成事件回调
	 */
	private IDownloadTaskFinishCallBack finishCallBack = new IDownloadTaskFinishCallBack() {

		@Override
		public void notifyDownloadThread() {
			if (downloadThread != null && tasks.size() > 0 && isWaiting) {
				// 唤醒任务下载队列
				synchronized (runnable) {
					runnable.notify();
				}
			}
		}
	};

	/**
	 * 获取任务
	 * 
	 * @param tid
	 * @return
	 */
	public DownloadTask getDownloadTask(String tid) {
		for (int i = 0; i < tasks.size(); i++) {
			DownloadTask temp = tasks.get(i);
			if (temp.getTid().equals(tid)) {
				return temp;
			}
		}
		return null;
	}

	/**
	 * 等待取消
	 */
	public void waitingCancel(String tid) {
		for (int i = 0; i < tasks.size(); i++) {
			DownloadTask temp = tasks.get(i);
			if (temp.getTid().equals(tid)) {
				tasks.remove(i);
				if (downloadThreadEventCallBack != null) {
					temp.setType(DownloadTask.DOWNLOAD_WAITING_CANCEL);
					downloadThreadEventCallBack.waitingCancel(temp);
				}
				break;
			}
		}
	}

	/**
	 * 添加在线任务
	 * 
	 * @param task
	 */
	public void addSingleDownloadTask(DownloadTask task) {
		while (tasks.size() > 0) {
			DownloadTask temp = tasks.get(0);
			DownloadThreadManage dtm = temp.getDownloadThreadManage();
			if (dtm.isFinish() || dtm.isCancel() || dtm.isError()
					|| dtm.isPause()) {
				tasks.remove(0);
			} else {
				dtm.cancel();
				tasks.remove(0);
			}
		}
		tasks.add(task);
		if (downloadThread == null) {
			downloadThread = new Thread(runnable);
			downloadThread.start();
		} else {
			// 唤醒任务下载队列
			synchronized (runnable) {
				runnable.notify();
			}
		}
	}

	/**
	 * 添加根据添加时间顺序来下载的任务
	 * 
	 * @param task
	 */
	public void addDownloadTaskByAddTime(DownloadTask task) {
		boolean flag = false;
		if (tasks.size() == 0) {
			flag = true;
		}
		if (!tasks.contains(task)) {
			int taskIndex = -1;
			int i = 0;
			for (; i < tasks.size(); i++) {
				DownloadTask temp = tasks.get(i);
				if (task.getAddTime().compareTo(temp.getAddTime()) > 0) {
					taskIndex = i;
					break;
				}
			}
			if (taskIndex == -1) {
				tasks.add(task);
			} else {
				if (tasks.size() >= taskIndex) {
					tasks.add(taskIndex, task);
				} else if (taskIndex - 1 < 0) {
					tasks.add(0, task);
				} else {
					tasks.add(taskIndex - 1, task);
				}
			}
			if (downloadThreadEventCallBack != null
					&& !task.getTid().equals(isDownloadingTid)
					&& tasks.size() != 1) {
				task.setType(DownloadTask.DOWNLOAD_WAITING);
				downloadThreadEventCallBack.waiting(task);
			}
			if (downloadThread != null && flag) {
				// 唤醒任务下载队列
				synchronized (runnable) {
					runnable.notify();
				}
			}
		} else {
			// 再次点击下载时，如果任务已经在列表中，则更新该下载任务的ui
			for (int i = 0; i < tasks.size(); i++) {
				DownloadTask temp = tasks.get(i);
				if (task.getTid().equals(temp.getTid())) {
					tasks.remove(i);
					tasks.add(i, task);
					if (downloadThreadEventCallBack != null
							&& !task.getTid().equals(isDownloadingTid)
							&& tasks.size() != 1) {
						task.setType(DownloadTask.DOWNLOAD_WAITING);
						downloadThreadEventCallBack.waiting(task);
					}
					break;
				}
			}
		}
		if (downloadThread == null) {
			downloadThread = new Thread(runnable);
			downloadThread.start();
		}
	}

	/**
	 * 添加任务
	 * 
	 * @param task
	 */
	public void addDownloadTask(DownloadTask task) {
		boolean flag = false;
		if (tasks.size() == 0) {
			flag = true;
		}
		if (!tasks.contains(task)) {
			// tasks.add(task);
			// 通过tid对任务进行排序，tid在服务器端添加时，要自动递增
			int taskIndex = -1;
			int i = 0;
			for (; i < tasks.size(); i++) {
				DownloadTask temp = tasks.get(i);
				// System.out.println(task.getTid() + "   " + temp.getTid());
				if (task.getTid().compareTo(temp.getTid()) > 0) {
					taskIndex = i;
					break;
				}
			}
			if (taskIndex == -1) {
				tasks.add(task);
			} else {
				if (tasks.size() >= taskIndex) {
					tasks.add(taskIndex, task);
				} else if (taskIndex - 1 < 0) {
					tasks.add(0, task);
				} else {
					tasks.add(taskIndex - 1, task);
				}
			}
			if (downloadThreadEventCallBack != null
					&& !task.getTid().equals(isDownloadingTid)
					&& tasks.size() != 1) {
				task.setType(DownloadTask.DOWNLOAD_WAITING);
				downloadThreadEventCallBack.waiting(task);
			}
			if (downloadThread != null && flag) {
				// 唤醒任务下载队列
				synchronized (runnable) {
					runnable.notify();
				}
			}
		} else {
			// 再次点击下载时，如果任务已经在列表中，则更新该下载任务的ui
			for (int i = 0; i < tasks.size(); i++) {
				DownloadTask temp = tasks.get(i);
				if (task.getTid().equals(temp.getTid())) {
					tasks.remove(i);
					tasks.add(i, task);
					if (downloadThreadEventCallBack != null
							&& !task.getTid().equals(isDownloadingTid)
							&& tasks.size() != 1) {
						task.setType(DownloadTask.DOWNLOAD_WAITING);
						downloadThreadEventCallBack.waiting(task);
					}
					break;
				}
			}
		}
		if (downloadThread == null) {
			downloadThread = new Thread(runnable);
			downloadThread.start();
		}
	}

	/**
	 * 任务线程
	 */
	private Runnable runnable = new Runnable() {

		public void run() {
			while (tasks.size() > 0) {
				DownloadTask task = tasks.get(0);
				DownloadThreadManage dtm = task.getDownloadThreadManage();
				dtm.setFinishCallBack(finishCallBack);
				if (downloadThreadEventCallBack != null) {
					dtm.setDownloadThreadEventCallBack(downloadThreadEventCallBack);
				}
				if (dtm.isFinish() || dtm.isCancel() || dtm.isError()
						|| dtm.isPause()) {
					tasks.remove(0);
					// 如果没有下载的任务，这里wait等待，方便后期添加任务
					if (tasks.size() == 0) {
						// 如果队列为空,则令线程等待
						synchronized (this) {
							try {
								isWaiting = true;
								this.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				} else {
					isWaiting = false;
					if (task.getType() == DownloadTask.SONG_NET) {
						dtm.startSingleTask();
					} else {
						dtm.start();
					}
					isDownloadingTid = task.getTid();
					// 如果队列为空,则令线程等待
					synchronized (this) {
						try {
							isWaiting = true;
							this.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

	};

	public void setDownloadThreadEventCallBack(
			IDownloadThreadEventCallBack downloadThreadEventCallBack) {
		this.downloadThreadEventCallBack = downloadThreadEventCallBack;
	}

	/**
	 * 任务完成事件回调
	 * 
	 * @author zhangliangming
	 * 
	 */
	public interface IDownloadTaskFinishCallBack {
		/**
		 * 唤醒下载任务线程
		 */
		public void notifyDownloadThread();
	}

	/**
	 * 下载任务回调事件
	 * 
	 * @author zhangliangming
	 * 
	 */
	public interface IDownloadThreadEventCallBack {
		/**
		 * 获取子线程的历史下载进度大小
		 * 
		 * @param task
		 *            任务
		 * @param threadID
		 *            线程id
		 * @param threadCount
		 *            线程数
		 * @return
		 */
		public int getThreadDownloadedSize(DownloadTask task, int threadID,
				int threadCount);

		/**
		 * 等待中
		 * 
		 * @param task
		 */
		public void waiting(DownloadTask task);

		/**
		 * 等待取消
		 * 
		 * @param task
		 */
		public void waitingCancel(DownloadTask task);

		/**
		 * 下载进度
		 * 
		 * @param task
		 * @param downloadedSize
		 *            下载进度
		 */
		public void downloading(DownloadTask task, int downloadedSize);

		/**
		 * * 子线程下载进度
		 * 
		 * @param task
		 * @param threadID
		 *            线程id
		 * @param threadCount
		 *            线程总数
		 * @param downloadedSize
		 *            下载进度
		 */
		public void threadDownloading(DownloadTask task, int threadID,
				int threadCount, int downloadedSize);

		/**
		 * 暂停
		 * 
		 * @param task
		 * @param downloadedSize
		 *            下载进度
		 */
		public void pauseed(DownloadTask task, int downloadedSize);

		/**
		 * 取消任务
		 * 
		 * @param task
		 */
		public void canceled(DownloadTask task);

		/**
		 * 下载完成
		 * 
		 * @param task
		 */
		public void finished(DownloadTask task);

		/**
		 * 下载错误
		 * 
		 * @param task
		 */
		public void error(DownloadTask task);
	}
}
