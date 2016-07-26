package com.jake.fileselect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity
{
    private TextView tvPaths;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvPaths = (TextView)findViewById(R.id.tvPaths);
    }

    public void btnSelectFile(View view)
    {
        Intent intent = new Intent(this, FileSelectActivity.class);
        intent.putExtra(FileSelectActivity.KEY_SINGLE_CHECK, false);
        startActivityForResult(intent, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == 10)
            {
                List<String> list = data.getStringArrayListExtra(FileSelectActivity.KEY_RESULT);
                StringBuilder sb = new StringBuilder();
                for (String path : list)
                {
                    sb.append(path).append("\n");
                }
                tvPaths.setText(sb.toString());
            }
        }
    }
}
