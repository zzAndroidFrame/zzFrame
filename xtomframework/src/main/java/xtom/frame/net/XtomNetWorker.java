package xtom.frame.net;

import java.util.ArrayList;

import org.json.JSONObject;

import xtom.frame.XtomConfig;
import xtom.frame.XtomObject;
import xtom.frame.exception.DataParseException;
import xtom.frame.exception.HttpException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * 网络请求发送器
 */
public class XtomNetWorker extends XtomObject {
	/**
	 * 请求成功(-1)
	 */
	protected static final int SUCCESS = -1;
	/**
	 * 请求异常(-2)
	 */
	public static final int FAILED_HTTP = -2;
	/**
	 * 数据异常(-3)
	 */
	public static final int FAILED_DATAPARSE = -3;
	/**
	 * 无网络(-4)
	 */
	public static final int FAILED_NONETWORK = -4;
	/**
	 * 获取数据前显示
	 */
	private static final int BEFORE = -5;

	private Context context;
	private EventHandler eventHandler;
	private NetThread netThread;
	private OnTaskExecuteListener onTaskExecuteListener;

	public XtomNetWorker(Context mContext) {
		Looper looper;
		if ((looper = Looper.myLooper()) != null) {
			eventHandler = new EventHandler(this, looper);
		} else if ((looper = Looper.getMainLooper()) != null) {
			eventHandler = new EventHandler(this, looper);
		} else {
			eventHandler = null;
		}

		this.context = mContext.getApplicationContext();
	}

	/**
	 * 发送post请求并且获取数据.该方法可发送文件数据
	 * 
	 * @param task
	 *            网络请求任务new XtomNetTask(任务ID,任务URL, 任务参数集(参数名,参数值))
	 */
	public void executeTask(XtomNetTask task) {
		if (hasNetWork()) {
			synchronized (this) {
				if (netThread == null) {
					netThread = new NetThread(task);
					netThread.start();
					log_d("网络线程不存在或已执行完毕,开启新线程：" + netThread.getName());
				} else {
					log_d(netThread.getName() + "执行中,添加网络任务");
					netThread.addTask(task);
				}
			}
		} else {
			if (onTaskExecuteListener != null) {
				onTaskExecuteListener.onPostExecute(this, task);
				onTaskExecuteListener.onExecuteFailed(this, task,
						FAILED_NONETWORK);
			}
		}
	}

	/**
	 * 判断网络任务是否都已完成
	 * 
	 * @return
	 */
	public boolean isNetTasksFinished() {
		synchronized (this) {
			return netThread == null || netThread.tasks.size() <= 0;
		}
	}

	/**
	 * 取消网络请求任务
	 */
	public void cancelTasks() {
		synchronized (this) {
			if (netThread != null)
				netThread.cancelTasks();
		}
	}

	/**
	 * 判断当前是否有可用网络
	 * 
	 * @return 如果有true否则false
	 */
	public boolean hasNetWork() {
		ConnectivityManager con = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = con.getActiveNetworkInfo();// 获取可用的网络服务
		return info != null && info.isAvailable();
	}

	private class NetThread extends Thread {
		private ArrayList<XtomNetTask> tasks = new ArrayList<XtomNetTask>();
		private boolean isRun = true;

		NetThread(XtomNetTask task) {
			tasks.add(task);
			setName("网络线程(" + getName() + ")");
		}

		void addTask(XtomNetTask task) {
			synchronized (XtomNetWorker.this) {
				tasks.add(task);
			}
		}

		void cancelTasks() {
			synchronized (XtomNetWorker.this) {
				tasks.clear();
				netThread = null;
				isRun = false;
			}
		}

		boolean isHaveTask() {
			return tasks.size() > 0;
		}

		@Override
		public void run() {
			log_d(getName() + "开始执行");
			while (isRun) {
				synchronized (XtomNetWorker.this) {
					if (!isHaveTask()) {
						isRun = false;
						netThread = null;
						break;
					}
				}
				XtomNetTask currTask = tasks.get(0);
				TR<XtomNetTask, Object> tr = new TR<XtomNetTask, Object>();
				tr.setTask(currTask);
				beforeDoTask(tr);
				Message mess = eventHandler.obtainMessage();
				doTask(tr, mess);
			}
			log_d(getName() + "执行完毕");
		}

		// 给handler发消息,执行请求任务前的操作
		private void beforeDoTask(TR<XtomNetTask, Object> result) {
			Message before = new Message();
			before.what = BEFORE;
			before.obj = result;
			eventHandler.sendMessage(before);
		}

		// 执行网络请求任务
		private void doTask(TR<XtomNetTask, Object> result, Message mess) {
			XtomNetTask task = result.getTask();
			log_d("Do task !!!Try " + (task.getTryTimes() + 1));
			log_d("The Task Description: " + task.getDescription());
			try {
				Object object;
				if (task.getFiles() == null) {
					JSONObject jsonObject = XtomHttpUtil.sendPOSTForJSONObject(
							task.getPath(), task.getParams(),
							XtomConfig.ENCODING);
					object = task.parse(jsonObject);
				} else {
					JSONObject jsonObject = XtomHttpUtil
							.sendPOSTWithFilesForJSONObject(task.getPath(),
									task.getFiles(), task.getParams(),
									XtomConfig.ENCODING);
					object = task.parse(jsonObject);
				}
				mess.obj = result.put(task, object);
				mess.what = SUCCESS;
				// mess.arg1 = task.getId();
				tasks.remove(task);
				eventHandler.sendMessage(mess);
			} catch (HttpException e) {
				tryAgain(task, FAILED_HTTP, mess, result);
			} catch (DataParseException e) {
				tryAgain(task, FAILED_DATAPARSE, mess, result);
			}
		}

		// 失败后再试几次
		private void tryAgain(XtomNetTask task, int type, Message mess,
				TR<XtomNetTask, Object> result) {
			task.setTryTimes(task.getTryTimes() + 1);
			if (task.getTryTimes() >= XtomConfig.TRYTIMES_HTTP) {
				mess.what = type;
				// mess.arg1 = task.getId();
				mess.obj = result;
				tasks.remove(task);
				eventHandler.sendMessage(mess);
			}
		}
	}

	public Context getContext() {
		return context;
	}

	public OnTaskExecuteListener getOnTaskExecuteListener() {
		return onTaskExecuteListener;
	}

	public void setOnTaskExecuteListener(
			OnTaskExecuteListener onTaskExecuteListener) {
		this.onTaskExecuteListener = onTaskExecuteListener;
	}

	private static class EventHandler extends Handler {
		private XtomNetWorker netWorker;

		public EventHandler(XtomNetWorker netWorker, Looper looper) {
			super(looper);
			this.netWorker = netWorker;
		}

		private OnTaskExecuteListener getOnTaskExecuteListener() {
			return netWorker.getOnTaskExecuteListener();
		}

		@Override
		public void handleMessage(Message msg) {
			OnTaskExecuteListener listener = getOnTaskExecuteListener();
			if (listener != null) {
				@SuppressWarnings("unchecked")
				TR<XtomNetTask, Object> result = (TR<XtomNetTask, Object>) msg.obj;
				switch (msg.what) {
				case SUCCESS:
					listener.onExecuteSuccess(netWorker, result.getTask(),
							result.getResult());
					listener.onPostExecute(netWorker, result.getTask());
					break;
				case FAILED_HTTP:
					listener.onExecuteFailed(netWorker, result.getTask(),
							FAILED_HTTP);
					listener.onPostExecute(netWorker, result.getTask());
					break;
				case FAILED_DATAPARSE:
					listener.onExecuteFailed(netWorker, result.getTask(),
							FAILED_DATAPARSE);
					listener.onPostExecute(netWorker, result.getTask());
					break;
				case BEFORE:
					listener.onPreExecute(netWorker, result.getTask());
					break;
				default:
					listener.onPostExecute(netWorker, result.getTask());
					break;
				}
			}
			super.handleMessage(msg);
		}
	}

	/**
	 * 网络请求任务和请求返回结果的对应关系
	 * 
	 * @param <Task>
	 *            网络请求任务
	 * @param <Result>
	 *            请求返回结果
	 */
	private class TR<Task, Result> {
		private Task t;
		private Result r;

		/**
		 * 实例化一个 网络请求任务和请求返回结果的对应关系
		 * 
		 * @param t
		 *            网络请求任务
		 * @param r
		 *            请求返回结果
		 * @return
		 */
		public TR<Task, Result> put(Task t, Result r) {
			setTask(t);
			setResult(r);
			return this;
		}

		/**
		 * 设置网络请求任务
		 * 
		 * @param t
		 *            网络请求任务实例
		 */
		public void setTask(Task t) {
			this.t = t;
		}

		/**
		 * 设置请求返回结果
		 * 
		 * @param r
		 *            请求返回结果实例
		 */
		public void setResult(Result r) {
			this.r = r;
		}

		/**
		 * 获取网络请求任务
		 * 
		 * @return 网络请求任务实例
		 */
		public Task getTask() {
			return t;
		}

		/**
		 * 获取请求返回结果
		 * 
		 * @return 请求返回结果实例
		 */
		public Result getResult() {
			return r;
		}
	}

	public interface OnTaskExecuteListener {
		/**
		 * Runs on the UI thread before the task run.
		 */
		public void onPreExecute(XtomNetWorker netWorker, XtomNetTask task);

		/**
		 * Runs on the UI thread after the task run.
		 */
		public void onPostExecute(XtomNetWorker netWorker, XtomNetTask task);

		/**
		 * Runs on the UI thread when the task run success.
		 * 
		 * @param result
		 *            the result of the server back.
		 */
		public void onExecuteSuccess(XtomNetWorker netWorker, XtomNetTask task,
				Object result);

		/**
		 * Runs on the UI thread when the task run failed.
		 * 
		 * @param failedType
		 *            the type of cause the task failed.
		 *            <p>
		 *            See {@link XtomNetWorker#FAILED_DATAPARSE
		 *            XtomNetWorker.FAILED_DATAPARSE},
		 *            {@link XtomNetWorker#FAILED_HTTP
		 *            XtomNetWorker.FAILED_HTTP},
		 *            {@link XtomNetWorker#FAILED_NONETWORK
		 *            XtomNetWorker.FAILED_NONETWORK}
		 *            </p>
		 */
		public void onExecuteFailed(XtomNetWorker netWorker, XtomNetTask task,
				int failedType);
	}

}
