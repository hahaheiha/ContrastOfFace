package com.cof.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.cof.StuFaceActivity;
import com.cof.MainActivity;
import com.cof.R;
import com.cof.utils.BitmapUtil;
import com.cof.utils.CustomerDialog;
import com.cof.utils.DatabaseHelper;
import com.cof.utils.ShowBigPhoto;

import java.util.List;

import com.cof.entity.StuFace;

public class StuFaceAdapter extends RecyclerView.Adapter<StuFaceAdapter.ViewHolder> {

    private List<StuFace> mStuList;
    private SQLiteDatabase db;
    private Context mContext;

    /**
     * 自定义类 ViewHolder，来减少 findViewById() 的使用
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        View stuView;
        ImageView stuImage;

        public ViewHolder(View view) {
            super(view);
            stuView = view;
            stuImage = (ImageView) view.findViewById(R.id.stuImage);
        }

    }

    /**
     * 构造函数
     * @param stuList
     */
    public StuFaceAdapter(List<StuFace> stuList) {
        mStuList = stuList;
    }

    /**
     * 得到数据库中学生的数据，为ImageView设置点击事件：查看｜删除
     * 并加载每个view的布局
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (db == null) {
            db = DatabaseHelper.getInstance(parent.getContext()).getWritableDatabase();
        }
        if (mContext == null) {
            mContext = parent.getContext();
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stu_face_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.stuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                StuFace stu = mStuList.get(position);
                Bitmap bitmap = null;
                Cursor cursor = db.rawQuery("select base64 from imagedb where imageid = ?", new String[]{stu.getImageId() + ""});
                if (cursor.moveToFirst()) {
                    String base64 = cursor.getString(cursor.getColumnIndex("base64"));
                    bitmap = BitmapUtil.stringtoBitmap(base64);
                }

                ShowBigPhoto showBigPhoto = new ShowBigPhoto(mContext);
                showBigPhoto.show();
                ImageView detailPhoto = (ImageView) showBigPhoto.findViewById(R.id.detailPhoto);
                detailPhoto.setImageBitmap(bitmap);

                detailPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showBigPhoto.dismiss();
                    }
                });

                Button detailCancelButton = (Button) showBigPhoto.findViewById(R.id.detailCancelButton);
                Button detailSureButton = (Button) showBigPhoto.findViewById(R.id.detailSureButton);

                detailCancelButton.setVisibility(View.INVISIBLE);

                detailSureButton.setVisibility(View.INVISIBLE);

                detailCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showBigPhoto.dismiss();
                    }
                });

                detailSureButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showBigPhoto.dismiss();
                    }
                });


            }
        });
        holder.stuImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                StuFace stu = mStuList.get(position);
                int imageId = stu.getImageId();

                Dialog dialog = new CustomerDialog(mContext, R.style.Dialog, R.layout.dialog);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                TextView dialogTitle = (TextView) dialog.findViewById(R.id.dialog_title);
                TextView dialogInfo = (TextView) dialog.findViewById(R.id.dialog_info);
                TextView dialogTextRight = (TextView) dialog.findViewById(R.id.dialog_text_right);
                TextView dialogTextLeft = (TextView) dialog.findViewById(R.id.dialog_text_left);

                dialogTextLeft.setText("取消");
                dialogTextLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialogTextRight.setText("确定");
                dialogTextRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.execSQL("delete from imagedb where imageid = " + imageId);
                        Intent intent = new Intent(mContext, StuFaceActivity.class);
                        intent.putExtra("deletePosition", position);
                        mContext.startActivity(intent);
                        Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

                dialogTitle.setText("删除");
                dialogInfo.setText("是否删除该图片？");
                return false;
            }
        });
        return holder;
    }

    /**
     * 设置每个ViewHolder中学生的图片
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (db == null) return;
//        if (deletePositionList.contains(position) || )
        StuFace stu = mStuList.get(position);
        int imageId = stu.getImageId();
//        Cursor query = db.query("imagedb", null, "where ? = " + imageId, new String[]{"imageid"}, null, null, null);
        Cursor cursor = db.rawQuery("select base64 from imagedb where imageid = ?", new String[]{imageId + ""});
        if (cursor.moveToFirst()) {
            String base64 = cursor.getString(cursor.getColumnIndex("base64"));
            Bitmap bitmap = BitmapUtil.stringtoBitmap(base64);
            holder.stuImage.setImageBitmap(bitmap);
//            holder.expName.setText(stu.getImgName());
        }
        else {
            holder.stuView.setVisibility(View.GONE);
        }
    }

    /**
     * 获取item数量
     * @return
     */
    @Override
    public int getItemCount() {
        return mStuList.size();
    }

}