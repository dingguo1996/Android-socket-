package chatclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.example.android_day01_chatclient.R;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private EditText etIp;
	private EditText etText;
	private ListView listView;
	private Button button;
	private List<String> list=new ArrayList<String>();
	
	// 用于通信的成员
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	//在主线程工作的handler
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HANDLER_RECEIVE_MESSAGE:
				//更新Adapter
				adapter.notifyDataSetChanged();
				break;
			case HANDLER_CONNECT_SUCCESS:
				Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
				//把按钮改成disable
				button.setEnabled(false);
				etIp.setEnabled(false);
				break;
			}
		}
	};
	private ArrayAdapter<String> adapter;
	
	public static final int HANDLER_CONNECT_SUCCESS=0;
	public static final int HANDLER_RECEIVE_MESSAGE=1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setViews();
		//设置适配器
		setAdapter();
	}

	private void setAdapter() {
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		listView.setAdapter(adapter);
	}

	// 控件初始化
	private void setViews() {
		etIp = (EditText) findViewById(R.id.etIp);
		etText = (EditText) findViewById(R.id.etText);
		listView = (ListView) findViewById(R.id.listView);
		button=(Button)findViewById(R.id.button1);
	}

	//
	public void doClick(View view) {
		switch (view.getId()) {
		case R.id.button1:
			// 建立于服务端的连接
			new Thread() {
				public void run() {
					try {
						connect();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
			break;
		case R.id.button2:
			//向服务端输出数据
			new Thread(){
				public void run() {
					try {
						writeToServer();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
			break;
		}
	}
	
	//向服务端写数据
	private void writeToServer() throws IOException {
		String text=etText.getText().toString();
		dos.writeUTF(text);
		dos.flush();
	}

	// 建立于服务端的连接
	private void connect() throws IOException {
		// NetWorkOnMainThreadException
		socket = new Socket(etIp.getText().toString(), 8888);
		//连接成功后  提示用户 把按钮disable
		handler.sendEmptyMessage(HANDLER_CONNECT_SUCCESS);
		// 获取输入输出流
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		//启动工作线程 
		//用于读取服务端写回客户端的信息
		new ReadThread().start();
	}
	
	class ReadThread extends Thread{
		public void run() {
			//不断的接收服务端写回客户端的数据
			try {
				while(true){
					String str=dis.readUTF();
					list.add(0, str);
					//发消息给handler
					handler.sendEmptyMessage(HANDLER_RECEIVE_MESSAGE);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
