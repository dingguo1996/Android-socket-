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
	
	// ����ͨ�ŵĳ�Ա
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	//�����̹߳�����handler
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HANDLER_RECEIVE_MESSAGE:
				//����Adapter
				adapter.notifyDataSetChanged();
				break;
			case HANDLER_CONNECT_SUCCESS:
				Toast.makeText(MainActivity.this, "���ӳɹ�", Toast.LENGTH_SHORT).show();
				//�Ѱ�ť�ĳ�disable
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
		//����������
		setAdapter();
	}

	private void setAdapter() {
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		listView.setAdapter(adapter);
	}

	// �ؼ���ʼ��
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
			// �����ڷ���˵�����
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
			//�������������
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
	
	//������д����
	private void writeToServer() throws IOException {
		String text=etText.getText().toString();
		dos.writeUTF(text);
		dos.flush();
	}

	// �����ڷ���˵�����
	private void connect() throws IOException {
		// NetWorkOnMainThreadException
		socket = new Socket(etIp.getText().toString(), 8888);
		//���ӳɹ���  ��ʾ�û� �Ѱ�ťdisable
		handler.sendEmptyMessage(HANDLER_CONNECT_SUCCESS);
		// ��ȡ���������
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		//���������߳� 
		//���ڶ�ȡ�����д�ؿͻ��˵���Ϣ
		new ReadThread().start();
	}
	
	class ReadThread extends Thread{
		public void run() {
			//���ϵĽ��շ����д�ؿͻ��˵�����
			try {
				while(true){
					String str=dis.readUTF();
					list.add(0, str);
					//����Ϣ��handler
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
