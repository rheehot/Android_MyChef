package thread.seopftware.mychef.Chatting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import thread.seopftware.mychef.R;

import static thread.seopftware.mychef.Login.Login_login.CHEFNORMALLEMAIL;
import static thread.seopftware.mychef.Login.Login_login.CHEFNORMALLOGIN;
import static thread.seopftware.mychef.Login.Login_login.FACEBOOKLOGIN;
import static thread.seopftware.mychef.Login.Login_login.FBAPI;
import static thread.seopftware.mychef.Login.Login_login.FBEMAIL;
import static thread.seopftware.mychef.Login.Login_login.FB_LOGINCHECK;
import static thread.seopftware.mychef.Login.Login_login.KAAPI;
import static thread.seopftware.mychef.Login.Login_login.KAEMAIL;
import static thread.seopftware.mychef.Login.Login_login.KAKAOLOGIN;
import static thread.seopftware.mychef.Login.Login_login.KAKAO_LOGINCHECK;

public class Chat_Client extends AppCompatActivity {

    private static String TAG="Chat_Client";
    BroadcastReceiver mReceiver;

    // 뷰
    EditText et_ChatInput;
    Button btn_Send;

    ListView listView;
    ListViewAdapter_Chat adapter;

    String Login_Email;
    String room_number;
    String Login_Name, Login_Image; // 로그인된 나의 이름 및 사진
    String Sender_Name = null, Sender_Image = null; // 로그인된 나의 이름 및 사진
    String content_time, content_message; // 메세지 시간 및 내용
    String email_receiver, email_sender; // 메세지를 받는 사람, 메세지를 보내는 사람
    String Receiver_name;
    SimpleDateFormat simpleDateFormat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_client);


        Intent intent1 = getIntent();
        email_receiver = intent1.getStringExtra("email");
        Receiver_name = intent1.getStringExtra("name");

        Log.d(TAG, "email_receiver : "+email_receiver+"Receiver_name : "+Receiver_name);

        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("com.dwfox.myapplication.SEND_BROAD_CAST");

        // 액션바 작업
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("채팅방");

        adapter=new ListViewAdapter_Chat();
        listView= (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        // 세션 유지를 위한 이메일 값 불러들이기
        SharedPreferences pref1 = getSharedPreferences(KAKAOLOGIN, MODE_PRIVATE);
        KAKAO_LOGINCHECK=pref1.getString(KAAPI, "0");

        SharedPreferences pref2 = getSharedPreferences(FACEBOOKLOGIN, MODE_PRIVATE);
        FB_LOGINCHECK=pref2.getString(FBAPI, "0");

        if(!FB_LOGINCHECK.equals("0")) {
            SharedPreferences pref = getSharedPreferences(FACEBOOKLOGIN, MODE_PRIVATE);
            Login_Email=pref.getString(FBEMAIL, "");
            Log.d(TAG, "FB chefemail: "+Login_Email);
        } else if(!KAKAO_LOGINCHECK.equals("0")) {
            SharedPreferences pref = getSharedPreferences(KAKAOLOGIN, MODE_PRIVATE);
            Login_Email=pref.getString(KAEMAIL, "");
            Log.d(TAG, "KA chefemail: "+Login_Email);
        } else { // 일반
            SharedPreferences pref = getSharedPreferences(CHEFNORMALLOGIN, MODE_PRIVATE);
            Login_Email=pref.getString(CHEFNORMALLEMAIL, "");
            Log.d(TAG, "Normal chefemail: "+Login_Email);
        }

        Log.d(TAG, "접속된 Email : "+Login_Email);

        getMyInfo(); // 채팅에 필요한 정보 가져오기

        // view 객체 선언
        btn_Send= (Button) findViewById(R.id.btn_Send);
        et_ChatInput= (EditText) findViewById(R.id.et_ChatInput);

        // editText를 통해서 입력받은 데이터를 서버에 전송
        btn_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content_message=et_ChatInput.getText().toString();

                if(content_message.length() == 0) {
                    Toast.makeText(getApplicationContext(), "메세지를 한 글자 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
                    et_ChatInput.requestFocus();
                    return;
                }

                if( content_message != null ) { // 만약 data가 비어있지 않다면 서버로 data 전송

                    long now=System.currentTimeMillis();
                    simpleDateFormat = new SimpleDateFormat("yyyyMMdd_hh:dd a");
                    Log.d("시간이 이상함", String.valueOf(simpleDateFormat));
                    String Show_Time = simpleDateFormat.format(new Date(now));
                    String[] time_split=Show_Time.split("_");
                    String Date = time_split[0];
                    String Time = time_split[1];
                    Log.d("시간 확인", "Date : "+Date+" Time : "+Time);

                    TimeCheckDB(room_number, Show_Time); // 그 방에서 가장 마지막으로 보낸 메세지의 날짜와 오늘의 날짜가 다르면 addTimeItem() 해주기

                    /*
                    *
                    * 메세지를 서비스로 보내는 곳
                    *
                    * */

                    try{

                        Log.d(TAG, "**************************************************");
                        Log.d(TAG, "btn_Send : 전송 버튼 클릭 시 메세지를 서비스로 날린다.");
                        Log.d(TAG, "**************************************************");

                        // 메세지를 서비스로 보내는 곳
                        JSONObject object = new JSONObject();
                        object.put("room_status", "1");
                        object.put("room_number", room_number);
                        object.put("email_sender", Login_Email);
                        object.put("content_message", et_ChatInput.getText().toString());
                        object.put("content_time", Show_Time);
                        String Object_Data = object.toString();

                        Intent intent = new Intent(Chat_Client.this, Chat_Service.class); // 액티비티 ㅡ> 서비스로 메세지 전달
                        intent.putExtra("command", Object_Data);
                        startService(intent);

                    } catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            }
        });


        /*
    *
    *
    * 서비스가 서버로부터 받은 메세지를 받는 곳
    *
    * */

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String getMessage = intent.getStringExtra("MessageFromService");

                Log.d(TAG, "getMessage : " + getMessage);


                Log.d(TAG, "**************************************************");
                Log.d(TAG, "5. BroadcastReceiver : 서비스에서 받은 메세지를 리스트뷰에 추가했습니다.");
                Log.d(TAG, "**************************************************");


                try {

                    // 여기서는 보내는 사람이 받는 사람이 됨. 헷갈리지 않기!! (내가 메세지를 보낼 때와z 받을 때가 구현되어 있어서 헷갈리기 쉽다.)
                    // 상대방이 보낸 메세지 JSON 임
                    JSONObject jsonObject = new JSONObject(getMessage);

                    String room_status = jsonObject.getString("room_status");
                    String room_number = jsonObject.getString("room_number");
                    String email_sender = jsonObject.getString("email_sender");
                    String content_message = jsonObject.getString("content_message");

                    Log.d("room_status", room_status);
                    Log.d("room_number", room_number);
                    Log.d("email_sender", email_sender);
                    Log.d("content_message", content_message);


                    long now=System.currentTimeMillis();
                    simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA);
                    Log.d("시간이 이상함", String.valueOf(simpleDateFormat));
                    String entrance_time = simpleDateFormat.format(new Date(now));

                    if(email_sender.equals(Login_Email)) {

                        Log.d("여긴 오나여", "email_sender.equals(Login_Email) 안");

                        if(room_status.equals("0")) {

                            Log.d("여긴 오나여222222", "email_sender.equals(Login_Email) 안");

                            adapter.addItemTime(entrance_time); // 맨 처음 접속시 날짜 띄우기
                            adapter.addItem(content_message); // ""님이 입장하셨습니다.
                            adapter.notifyDataSetChanged();

                        }


                    } else {
                        if(room_status.equals("0")) { // 채팅방 최초 접속


                            adapter.addItemTime(entrance_time); // 맨 처음 접속시 날짜 띄우기
                            adapter.addItem(content_message); // ""님이 입장하셨습니다.
                            adapter.notifyDataSetChanged();

                        } else if(room_status.equals("1")) { // 채팅 메세지

                            String content_time = jsonObject.getString("content_time");
                            Log.d("content_time", content_time);

                            senderInfoDB(room_status, room_number, email_sender, content_message, content_time); // 보내는 사람의 이메일 값을 DB로 보낸 다음 닉네임/프로필 사진 받아옴



                        } else if(room_status.equals("2")) {

                            adapter.addItem(content_message); // ""님이 나가셨습니다.
                            adapter.notifyDataSetChanged();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        };

        registerReceiver(mReceiver, intentfilter);
    }






    //액션바 백키 버튼 구현
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {

                Log.d(TAG, "~메세지 보내서 ~님이 나갔습니다. 메세지 띄우기 STATUS-0 , content_message 가 달라짐");
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }



    // 보내는 사람 이메일 값 보내고 이름 및 프로필 사진 받아오기
    // 프로필 정보 받아오는 함수
    private void getMyInfo() {

        String url = "http://115.71.239.151/Chatting_Information.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("getChattingInfo parsing", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("result");

                    JSONObject jo = jsonArray.getJSONObject(0);

                    // 데이터 불러들이기
                    Login_Name=jo.getString("name"); // 0
                    Login_Image=jo.getString("profile"); // 1

                    Log.d(TAG, "Login_Name : "+Login_Name+"Login_Image : "+Login_Image);

                    chat_getRoom();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Anything you want
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

//                Log.d(TAG, "email_sender : "+email_sender);
                Map<String,String> map = new Hashtable<>();
                map.put("Login_Email", Login_Email);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    // 나의 입장에서 보내는 사람의 이메일 주소
    private void senderInfoDB(final String room_status1, final String room_number1, final String email_sender1, final String content_message1, final String content_time1) {

        String url = "http://115.71.239.151/Chatting_SenderInfo.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("senderInfoDB parsing", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("result");

                    JSONObject jo = jsonArray.getJSONObject(0);

                    // 데이터 불러들이기
                    Sender_Name=jo.getString("name"); // 0
                    Sender_Image=jo.getString("profile"); // 1
                    Log.d(TAG, "senderInfoDB Sender_Name : "+Sender_Name+"senderInfoDB Sender_Image : "+Sender_Image);


                    Log.d(TAG, "리스트뷰 추가전 Sender_Name :"+Sender_Name+"리스트뷰 추가전 Sender_Image : "+Sender_Image);
                    adapter.addItem(email_sender1, Sender_Name, content_time1, content_message1, "http://115.71.239.151/"+Sender_Image);
                    adapter.notifyDataSetChanged();
                    chat_SaveMessage(room_status1, room_number1, email_sender1, content_message1, content_time1);
                    // 여기서 Login_Name은 자기 이름임. 보내는 사람의 이메일을 웹서버로 보낸 다음 그 값을 기반으로 닉네임/이미지를 불러와야함.

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Anything you want
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Log.d(TAG, "나의 입장에서 보내는 사람의 이메일 주소 email_sender : "+email_sender1);
                Map<String,String> map = new Hashtable<>();
                map.put("email_sender", email_sender1);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void chat_getRoom() {

        String url = "http://115.71.239.151/chat_CreateRoom.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("chat_CreateRoom (룸 번호)", response); // column 갯수로 현재 룸 갯수를 구할 수 있음. 이 값을 채팅화면으로 넘긴다.
                room_number = response;

                try{

                    Log.d(TAG, "**************************************************");
                    Log.d(TAG, "1. 액티비티에서 서비스로 메세지를 보넀습니다.");
                    Log.d(TAG, "**************************************************");

                    // 메세지를 서비스로 보내는 곳
                    JSONObject object = new JSONObject();
                    object.put("room_status", "0");
                    object.put("room_number", room_number);
                    object.put("email_receiver", email_receiver);
                    object.put("email_sender", Login_Email);
                    object.put("content_message", Receiver_name+"이 입장했습니다.");
                    String Object_Data = object.toString();

                    Intent intent = new Intent(Chat_Client.this, Chat_Service.class); // 액티비티 ㅡ> 서비스로 메세지 전달
                    intent.putExtra("command", Object_Data);
                    startService(intent);


                } catch (JSONException e){
                    e.printStackTrace();
                }



//                getChattingContent();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Log.d("chat_getRoom", "Login_Email :" +Login_Email + "email_receiver : " +email_receiver);
                Map<String,String> map = new Hashtable<>();
                map.put("email_sender", Login_Email);
                map.put("email_receiver", email_receiver);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void getChattingContent() {

        String url = "http://115.71.239.151/chat_CreateRoom.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("chat_CreateRoom (룸 번호)", response); // column 갯수로 현재 룸 갯수를 구할 수 있음. 이 값을 채팅화면으로 넘긴다.
                room_number = response;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String,String> map = new Hashtable<>();
                map.put("email_sender", Login_Email);
                map.put("email_receiver", email_receiver);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void chat_SaveMessage(final String room_status, final String room_number, final String email_sender, final String content_message, final String content_time) {

        String url = "http://115.71.239.151/chat_SaveMessage.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("chat_SaveMessage", response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Anything you want
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

//                Log.d(TAG, "email_sender : "+email_sender);
                Map<String,String> map = new Hashtable<>();
                map.put("room_status", room_status);
                map.put("room_number", room_number);
                map.put("email_sender", email_sender);
                map.put("content_message", content_message);
                map.put("content_time", content_time);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void TimeCheckDB(final String room_number, final String content_time) { // 방 번호를 보낸 다음 현재 시간과 마지막 메세지 시간을 비교. 날짜가 다르면 addTimeItem() 해주기.

        String url = "http://115.71.239.151/Chatting_TimeCheck.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("TimeCheckDB", response);

                // 마지막 메세지를 보낸 날짜와 현재의 시간을 비교. 날짜가 다르면 addTimeItem() / 같으면 addItem()만 해주기

                long now=System.currentTimeMillis();
                simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 E요일_hh:dd a", Locale.KOREA);
                Log.d("시간이 이상함", String.valueOf(simpleDateFormat));
                String Show_Time = simpleDateFormat.format(new Date(now));

                String[] time_split=Show_Time.split("_");
                String Date = time_split[0];
                String Time = time_split[1];
                Log.d("시간 확인", "Date : "+Date+" Time : "+Time);

                if(response.equals("0")) { // 마지막으로 메세지를 보낸 날짜와 지금 메세지를 보낸 날짜가 같을 때 + 메세지만 추가
                    adapter.addItem(Login_Email, Login_Name, Time, content_message, "http://115.71.239.151/"+Login_Image); // 서버 보내지 않고도 자체적으로 ListView에 띄우기
                    chat_SaveMessage("0", room_number, Login_Email, content_message, content_time);

                    Log.d(TAG, "TimeCheckDB 저장값 room_number : "+room_number+" email_sender : " +Login_Email+" content_message : " + content_message+" content_time : "+content_time);
                    et_ChatInput.setText("");
                } else if (response.equals("1")) { // 마지막으로 메세지를 보낸 날짜와 지금 메세지를 보낸 날짜가 다를 때 + 메세지/날짜 추가
                    adapter.addItemTime(Date);
                    adapter.addItem(Login_Email, Login_Name, Time, content_message, "http://115.71.239.151/"+Login_Image); // 서버 보내지 않고도 자체적으로 ListView에 띄우기
                    et_ChatInput.setText("");

                    chat_SaveMessage("0", room_number, Login_Email, content_message, content_time);
                    Log.d(TAG, "TimeCheckDB 저장값 room_number : "+room_number+" email_sender : " +Login_Email+" content_message : " + content_message+" content_time : "+content_time);
                }

                adapter.notifyDataSetChanged();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Anything you want
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

//                Log.d(TAG, "email_sender : "+email_sender);
                Map<String,String> map = new Hashtable<>();
                map.put("room_number", room_number);
                map.put("content_time", content_time);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

}





/*
 메인

try {
            Socket c_socket = new Socket("115.71.239.151", 8888);

            Log.d(TAG, "서버와 연결되었습니다.");

            ReceiveThread rec_thread = new ReceiveThread();
            rec_thread.setSocket(c_socket);

            SendThread send_thread = new SendThread();
            send_thread.setSocket(c_socket);

            rec_thread.start();
            send_thread.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/




/*
    // 서버로 부터 데이터를 받는 쓰레드
    public class ReceiveThread extends Thread {
        private Socket m_Socket;

        @Override
        public void run() {
            super.run();

            try {

                networkReader = new BufferedReader(new InputStreamReader(m_Socket.getInputStream()));

                String receiveString;
                String[] split;

                while (true) {
                    receiveString = networkReader.readLine();

                    split = receiveString.split(">");

                    if(split.length >= 2 && split[0].equals(Chat_Client.UserID)) {
                        continue;
                    }

                    Log.d(TAG, "Receive Thread (receiveString) : "+receiveString);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setSocket(Socket _socket) {
            m_Socket = _socket;
        }
    }

    // 서버로 데이터를 전송하는 쓰레드
    public class SendThread extends Thread {
        private Socket m_Socket;

        @Override
        public void run() {
            super.run();

            try{

                networkReader = new BufferedReader(new InputStreamReader(System.in));

                networkWriter = new PrintWriter(m_Socket.getOutputStream());

                String sendString;

                Log.d(TAG, "사용할 아이디 입력");
                Chat_Client.UserID = networkReader.readLine();

                networkWriter.println("IDhighkrs12345" + Chat_Client.UserID);
                networkWriter.flush();

                while (true) {
                    sendString = networkReader.readLine();

                    if(sendString.equals("exit")) {
                        break;
                    }

                    networkWriter.println(sendString);
                    networkWriter.flush();
                }

                networkWriter.close();
                networkReader.close();
                m_Socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setSocket(Socket _socket) {
            m_Socket = _socket;
        }
    }
 */