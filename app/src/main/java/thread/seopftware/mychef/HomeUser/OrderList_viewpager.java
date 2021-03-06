package thread.seopftware.mychef.HomeUser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import thread.seopftware.mychef.Chatting.User_Reply;
import thread.seopftware.mychef.HomeChef.Home_Foodlook;
import thread.seopftware.mychef.R;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.ACTION_DIAL;
import static com.facebook.FacebookSdk.getApplicationContext;
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

public class OrderList_viewpager extends ListFragment {

    private static String TAG="OrderList_viewpager";
    public static final String EMAIL_SENDER = "Email_SenderKey";
    ListViewAdapter_User_ViewPager adapter;
    ListViewItem_User_ViewPager listViewItem_menu;
    ArrayList<ListViewItem_User_ViewPager> listViewItemList;
    String Id; // 음식 메뉴 고유 id

    String SearchWord;
    String UserEmail, Chef_Email, Chef_Name;

    TextView Food_Id, Chef_Number, tv_Food_Name, tv_Chef_Name, tv_Chef_Email;
    String Food_Name, foodid;

    int pos;
    long longid;


    public OrderList_viewpager() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        SharedPreferences pref1 = getContext().getSharedPreferences(KAKAOLOGIN, MODE_PRIVATE);
        KAKAO_LOGINCHECK=pref1.getString(KAAPI, "0");

        SharedPreferences pref2 = getContext().getSharedPreferences(FACEBOOKLOGIN, MODE_PRIVATE);
        FB_LOGINCHECK=pref2.getString(FBAPI, "0");

        if(!FB_LOGINCHECK.equals("0")) {
            SharedPreferences pref = getContext().getSharedPreferences(FACEBOOKLOGIN, MODE_PRIVATE);
            UserEmail=pref.getString(FBEMAIL, "");
            Log.d(TAG, "FB chefemail: "+UserEmail);
        } else if(!KAKAO_LOGINCHECK.equals("0")) {
            SharedPreferences pref = getContext().getSharedPreferences(KAKAOLOGIN, MODE_PRIVATE);
            UserEmail=pref.getString(KAEMAIL, "");
            Log.d(TAG, "KA chefemail: "+UserEmail);
        } else { // 일반
            SharedPreferences pref = getContext().getSharedPreferences(CHEFNORMALLOGIN, MODE_PRIVATE);
            UserEmail=pref.getString(CHEFNORMALLEMAIL, "");
            Log.d(TAG, "Normal chefemail: "+UserEmail);
        }
        Log.d(TAG, "UserEmail : "+UserEmail);

        ParseDB();

        super.onActivityCreated(savedInstanceState);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                pos=position;
                longid=id;

                Chef_Number = (TextView) view.findViewById(R.id.tv_Chef_Number);
                tv_Food_Name = (TextView) view.findViewById(R.id.tv_Food_Name);
                Food_Id= (TextView) view.findViewById(R.id.tv_Food_Id);
                tv_Chef_Name= (TextView) view.findViewById(R.id.tv_Chef_Name);
                tv_Chef_Email= (TextView) view.findViewById(R.id.tv_Chef_Email);


                final CharSequence[] items=new CharSequence[] {"쉐프에게 전화걸기", "쉐프에게 1:1 문의 요청", "출장 완료","예약 취소하기"};
                AlertDialog.Builder dialog=new AlertDialog.Builder(getContext());
                dialog.setTitle("MENU");
                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(items[which]=="쉐프에게 전화걸기") {

                            String Number=Chef_Number.getText().toString();

                            Log.e("listview","Chef_Number 값 : "+Number);

                            Intent intent=new Intent(ACTION_DIAL, Uri.parse("tel:"+Number)); // 전화문의
                            startActivity(intent);
                        }

                        if(items[which]=="쉐프에게 1:1 문의 요청") {

/*                            SharedPreferences pref = getContext().getSharedPreferences(EMAIL_SENDER, MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();

                            Log.d("1ㅣ1문의 값", "Chef_Email : "+Chef_Email+ "Chef_Name : "+ Chef_Name);
                            editor.putString("EMAIL", Chef_Email);
                            editor.putString("NAME", Chef_Name);
                            editor.commit();*/


                            String Chef_Name = tv_Chef_Name.getText().toString();
                            String Chef_Email = tv_Chef_Email.getText().toString();

                            Log.d (TAG, "Chef_Name : "+Chef_Name+" Chef_Email : "+Chef_Email);

                            // POST: 이메일 주소 값을 보내고  (쉐프메일+유저메일)
                            // GET: 방 번호
                            // 만약에 쉐프메일+유저메일이 존재하면 방번호 가져오고, 만약 존재하지 않으면 방번호 생성 후 가져온다
                            getRoomNumberDB(Chef_Email, UserEmail, Chef_Name);

                        }

                        if(items[which]=="출장 완료") {
                            // Status 상태 변경
                            Food_Name=tv_Food_Name.getText().toString();
                            foodid=Food_Id.getText().toString();

                            Toast.makeText(getContext(), "출장 완료를 클릭하셨습니다.\n쉐프의 요리실력을 평가해 주세요.", Toast.LENGTH_SHORT).show();
                            Order_Confirm(); // status 1

                            Bundle bundle=new Bundle();
                            Intent intent=new Intent(getContext(), User_Reply.class);
                            bundle.putString("Food_Id", foodid);
                            bundle.putString("Food_Name", Food_Name);
                            bundle.putString("User_Email", UserEmail);
                            intent.putExtras(bundle);
                            startActivity(intent);

                            for(int i=0; i<listViewItemList.size();i++){
                                if(listViewItemList.get(pos).equals(listViewItemList.get(i))){
                                    listViewItemList.remove(pos);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }

                        if(items[which] == "예약 취소하기") {
                            Toast.makeText(getContext(), "예약 취소하기를 클릭하셨습니다.\n예약 취소 절차가 진행 됩니다.", Toast.LENGTH_SHORT).show();
                            Food_Name=tv_Food_Name.getText().toString();
                            Order_Cancel(); // status 2

                            for(int i=0; i<listViewItemList.size();i++){
                                if(listViewItemList.get(pos).equals(listViewItemList.get(i))){
                                    listViewItemList.remove(pos);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
                dialog.show();
                return true;
            }
        });
    }

    // 한번 클릭 시 해당 메뉴 상세 보기 화면 으로
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Food_Id = (TextView) v.findViewById(R.id.tv_Food_Id);
        String Id=Food_Id.getText().toString();
        Log.e("listview","food menu id 값 : "+Id);

        Intent intent=new Intent(getContext(), Home_Foodlook.class);
        intent.putExtra("Id", Id);
        startActivity(intent);

        super.onListItemClick(l, v, position, id);
    }

    // db 데이터 로드
    private void ParseDB() {

        String url = "http://115.71.239.151/Orderlist_User_Parsing1.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("parsing1", response);

                try {
                    listViewItemList = new ArrayList<ListViewItem_User_ViewPager>();

                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("result");

                    if(jsonArray.length()==0) {
                        Toast.makeText(getContext(), "예약 중이신 출장 계획이 없습니다.\n예약을 먼저 진행해 주세요.", Toast.LENGTH_SHORT).show();
                        setListShown(true);
                        return;

                    } else {
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jo = jsonArray.getJSONObject(i);

                            String Chef_Name = jo.getString("Chef_Name");
                            String Chef_Profile = jo.getString("Food_Image");
                            String Chef_Phone=jo.getString("Chef_Phone");
                            String Chef_Email=jo.getString("Chef_Email");

                            Log.d(TAG, "Chef_Email"+Chef_Email);

                            String Food_Id=jo.getString("Food_Id");
                            String Food_Name = jo.getString("Food_Name");
                            String Food_Count= jo.getString("Food_Count");
                            String Food_Date = jo.getString("Food_Date");
                            String Food_Time = jo.getString("Food_Time");
                            String Food_Place = jo.getString("Food_Place");

                            listViewItem_menu = new ListViewItem_User_ViewPager();
                            listViewItem_menu.setChef_Name(Chef_Name+" 쉐프님");
                            listViewItem_menu.setChef_Number(Chef_Phone+" 쉐프님");

                            listViewItem_menu.setChef_Email(Chef_Email);
                            listViewItem_menu.setFood_Id(Food_Id);
                            listViewItem_menu.setFood_Name(Food_Name);
                            listViewItem_menu.setFood_Count(Food_Count+" (인분)");
                            listViewItem_menu.setFood_Date(Food_Date);
                            listViewItem_menu.setFood_Time(Food_Time);
                            listViewItem_menu.setFood_Place(Food_Place);
                            listViewItem_menu.setChef_Profile("http://115.71.239.151/" + Chef_Profile);
                            listViewItemList.add(listViewItem_menu);

                        }
                        adapter = new ListViewAdapter_User_ViewPager(listViewItemList);
                        setListAdapter(adapter);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String,String> map = new Hashtable<>();
                map.put("User_Email", UserEmail);
                map.put("Status", "0");

                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void Order_Confirm() { // Status의 상태를 1로 변경 (Food 이름 똑같은 것 찾아서)

        String url = "http://115.71.239.151/Orderlist_User_Confirm.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("parsing1", response);
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String,String> map = new Hashtable<>();
                Log.d(TAG, "Food_Name (confirm)" +Food_Name);
                map.put("Food_Name", Food_Name);

                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void Order_Cancel() { // Status의 상태를 2로 변경 (Food 이름 똑같은 것 찾아서)

        String url = "http://115.71.239.151/Orderlist_User_Cancel.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("parsing1", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String,String> map = new Hashtable<>();
                Log.d(TAG, "Food_Name (cancel)" +Food_Name);
                map.put("Food_Name", Food_Name);

                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    // POST: 이메일 주소 값을 보내고  (쉐프메일+유저메일)
    // GET: 방 번호
    // 만약에 쉐프메일+유저메일이 존재하면 방번호 가져오고, 만약 존재하지 않으면 방번호 생성 후 가져온다
    private void getRoomNumberDB(final String chef_email, final String user_email, final String chef_name) {

        String url = "http://115.71.239.151/chat_CreateRoom.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "getRoomNumberDB response (room_number): "+response);

                final String room_number = response;

                Toast.makeText(getContext(), "1:1문의 요청 완료!", Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new Hashtable<>();
                map.put("chef_email", chef_email);
                map.put("user_email", user_email);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }


    @Override
    public void onResume() {
        Log.d(this.getClass().getSimpleName(), "onResume()");
        super.onResume();

        ParseDB(); // db 데이터 삽입
    }
}
