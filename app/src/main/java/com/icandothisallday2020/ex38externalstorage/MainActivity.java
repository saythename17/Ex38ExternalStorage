package com.icandothisallday2020.ex38externalstorage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {
    EditText et;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et=findViewById(R.id.et);
        tv=findViewById(R.id.tv);
    }

    public void clickSave(View view) {
        //외장메모리(SD Card)가 있는지 확인
        String state=Environment.getExternalStorageState();
        //외장메모리 상태(state) 연결(mounted)/mounted 확인
        if(!state.equals(Environment.MEDIA_MOUNTED)){//=="unmounted"
            Toast.makeText(this,"SD card is not mounted",Toast.LENGTH_SHORT).show();
            return;//(clickSave method 종료)
        }
        String data=et.getText().toString();
        et.setText("");
        //데이터를 저장할 파일의 경로 얻어오기
        //외부메모리의 앱에게 할당된 고유한 폴더(저장)경로 얻어오기
        File[] dirs=getExternalFilesDirs("MyDir");//지정한 폴더이름
        File path=dirs[0];//첫번째 경로 선택
        tv.setText(path.getPath());//위에서 선택한 경로 set on tv
        //path(경로)와  파일명을 결합한 File 객체 생성
        File file=new File(path,"Data.txt");
        try {
            FileWriter fw=new FileWriter(file,true);//true:이어붙이기 모드
            PrintWriter writer=new PrintWriter(fw);//보조 writer
            writer.println(data); writer.flush(); writer.close();
            Toast.makeText(this,"Saved",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {}
    }

    public void clickLoad(View view) {
        String state=Environment.getExternalStorageState();
        if(state.equals("mounted")||state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
                                                    //└읽기기능 전용
            File[] dirs=getExternalFilesDirs("MyDir");
            File path=dirs[0];
            File file=new File(path,"Data.txt");
            try {
                FileReader fr= new FileReader(file);
                BufferedReader br=new BufferedReader(fr);
                StringBuffer buffer=new StringBuffer();
                while (true){
                    String line=br.readLine();
                    if(line==null) break;
                    buffer.append(line+"\n");
                }
                tv.setText(buffer.toString());
            } catch (Exception e) {}
        }
    }
    //requestPermission()를 실행해 나온 다이얼로그의 허가/거부 선택시
    //자동으로 실행되는 콜백메소드
    //≒ startActivityForResult()->...onCreateActivityResult() 자동호출되는 것과 같음
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 17:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "저장 가능", Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, "외부저장소 사용불가", Toast.LENGTH_SHORT).show();
        }
    }

    public void clickPATH(View view) {
        String state=Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this,"SD card is unmounted",Toast.LENGTH_SHORT).show();
            return;
        }//api 23(M)ver~이상 : 외부메모리에서 할당받은 고유경로가 아닌곳을 사용할 때※퍼미션 필요
        //동적퍼미션 작업(앱 실행중에 다이얼로그가 보이면서 퍼미션 체크)
        if(Build.VERSION.SDK_INT>=23) {//앱에서 저장소를 사용하는 퍼미션이 허가되어 있는지 체크
            //                     └Build.VERSION_CODES.M (상수)로도 사용가능
            int checkResult=checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            //외부저장소 사용허가가 거부(denied) 되었는지 확인
            if(checkResult== PackageManager.PERMISSION_DENIED){
                //granted :허가되다==0 denied :거부되다==-1
                //안드로이드에서 설정된 퍼미션 허용요청 다이얼로그를 생성 메소드 실행
                String[] permissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions,17);//파라미터: String 배열,식별번호
                return;
            }
        }
        //퍼미션이 허가된 이후에 실행되는 영역
        //SD card 내 특정위치에 저장
        File path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file=new File(path,"aaa.txt");
        try {
            FileWriter fr=new FileWriter(file,true);
            PrintWriter writer=new PrintWriter(fr);
            writer.println(et.getText().toString());
            writer.flush(); writer.close();
            tv.setText("Saved");
        } catch (IOException e) {}
    }
}
