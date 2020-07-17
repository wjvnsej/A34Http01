package com.kosmo.a34http01;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    final String TAG = "KOSMO123";
    TextView textResult;
    ProgressDialog dialog;//서버와 통신중 띄어줄 진행대화창
    int buttonResId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textResult = (TextView)findViewById(R.id.text_result);
        Button btnJson = (Button)findViewById(R.id.btn_json);
        btnJson.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                buttonResId = view.getId();
                /*
                회원리스트 가져오기 버튼을 누를경우 객체를 생성하고,
                execute()메소드를 즉시 호출한다. 이때 파라미터는 1개만 전달한다.
                 */
                new AsyncHttpRequest().execute("http://192.168.219.110:8282/k12springapi/android/memberList.do");
            }
        });

        //서버와 통신시 진행대화창을 띄우기 위한 준비
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//스타일설정
        dialog.setIcon(android.R.drawable.ic_dialog_alert);//아이콘설정
        dialog.setTitle("회원정보 리스트 가져오기");//타이틀설정
        dialog.setMessage("서버로부터 응답을 가디리고 있습니다.");//내용설정
        dialog.setCancelable(false);//back키로 닫히지 않도록 설정
    }
    class AsyncHttpRequest extends AsyncTask<String, Void, String>{

        // doInBackground() 실행 전 호출
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            //진행창이 만약 없다면 띄워준다.(서버와 통신이 끝날 때 까지 띄워진다.)
            if(!dialog.isShowing())
                dialog.show();
        }

        /*
        execute()를 호출할 때 전달된 2개의 파라미터를 가변인자가 받음.
        사용시에는 배열로 사용한다.
         */
        @Override
        protected String doInBackground(String... strings){

            /*
            서버에서 반환하는 JSON데이터를 저장 할 변수
             */
            StringBuffer sBuffer = new StringBuffer();

            try{
                //파라미터의 첫번째 값은 서버의 요청 URL로 서버와 연결한다.
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST"); //POST 방식으로 통신
                connection.setDoOutput(true);/*
                    OutputStream 으로 파라미터를 전달할 때 POST 방식으로 설정하겠다는 뜻으로
                    만약 위에서 GET으로 설정하더라도 해당 코드가 추가되면 POST 방식으로 변경된다.
                */

                /*
                요청 파라미터 설정
                    : 파라미터는 쿼리스트링 형태로 지정된다.
                    한글의 경우 URL 인코딩을 해야한다.
                 */
                OutputStream out = connection.getOutputStream();
                /*
                out.write(strings[1].getBytes());
                파라미터가 2개 이상이라면 &로 문자열을 연결
                out.write("&".getBytes());
                out.write(strings[2].getBytes());
                */

                out.flush();
                out.close();

                //서버에 요청이 전달되고 성공이라면 HTTP_OK로 확인 가능함
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){
                    Log.i(TAG, "HTTP OK 성공");

                    //서버로부터 받은 응답내용을 한줄씩 읽어서 StringBuffer 에 저장한다.
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    String responseData;

                    while((responseData=reader.readLine())!=null){
                        sBuffer.append(responseData+"\n\r");
                    }
                    reader.close();
                }
                else{
                    //서버와 연결 실패인 경우..
                    Log.i(TAG,"HTTP OK 안됨");
                }

                //버튼이 "회원리스트가져오기" 라면 JSON 파싱
                if(buttonResId ==R.id.btn_json){

                    Log.i(TAG, sBuffer.toString());
                    //서버의 응답 데이터인 JSON 을 파싱한다.
                    JSONArray jsonArray = new JSONArray(sBuffer.toString());
                    sBuffer.setLength(0); //StringBuffer 를 초기화
                    //JSON 배열의 크기만큼 반복하면서 파싱된 데이터를 저장한다.
                    for(int i=0; i<jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        sBuffer.append("아이디:"+ jsonObject.getString("id")+"\n\r");
                        sBuffer.append("패스워드:"+ jsonObject.getString("pass")+"\n\r");
                        sBuffer.append("이름:"+ jsonObject.getString("name")+"\n\r");
                        sBuffer.append("가입날짜:"+ jsonObject.getString("regidate")+"\n\r");
                        sBuffer.append("------------------\n\r");

                    }
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
            /*
            파싱이 완료된 StringBuffer 객체를 문자열로 반환하여 반환한다.
            여기서 반환된 값은 onPostExecute()로 전달된다.
             */
            return  sBuffer.toString();
        }

        //doInBackground()가 정상 종료되면 해당 함수가 호출된다.
        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            //진행대화창을 닫아주고..
            dialog.dismiss();
            //파싱된 결과데이터를 TextView 에 출력한다.
            textResult.setText(s);
        }
    }
}