package adapter;

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


import com.cof.ExpressionActivity;
import com.cof.MainActivity;
import com.cof.R;
import com.cof.utils.BitmapUtil;
import com.cof.utils.CustomerDialog;
import com.cof.utils.DatabaseHelper;
import com.cof.utils.ShowBigPhoto;


import java.util.ArrayList;
import java.util.List;

import entity.Expression;

public class ExpressionAdapter extends RecyclerView.Adapter<ExpressionAdapter.ViewHolder> {

    private List<Expression> mExpList;
//    private static DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Context mContext;
    private ArrayList<Integer> deletePositionList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View expView;
        ImageView expImage;
        TextView expName;

        public ViewHolder(View view) {
            super(view);
            expView = view;
            expImage = (ImageView) view.findViewById(R.id.expImage);
//            expName = (TextView) view.findViewById(R.id.expName);

        }

    }

    public ExpressionAdapter(List<Expression> expList) {
        mExpList = expList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (db == null) {
            db = DatabaseHelper.getInstance(parent.getContext()).getWritableDatabase();
        }
        if (mContext == null) {
            mContext = parent.getContext();
        }
        deletePositionList = new ArrayList<>();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exp_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.expView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Expression exp = mExpList.get(position);
                Intent intent = new Intent(parent.getContext(), MainActivity.class);
//                intent.putExtra(ExpressionActivity.EXP_NAME, exp.getImgName());
                intent.putExtra(ExpressionActivity.EXP_IMAGE_ID, exp.getImageId());
//                mContext.startActivity(intent);
                parent.getContext().startActivity(intent);
            }
        });
        holder.expImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Expression exp = mExpList.get(position);
                Bitmap bitmap = null;
                Cursor cursor = db.rawQuery("select base64 from imagedb where imageid = ?", new String[]{exp.getImageId() + ""});
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
        holder.expImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                Expression exp = mExpList.get(position);
                int imageId = exp.getImageId();

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
                        Intent intent = new Intent(mContext, ExpressionActivity.class);
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
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (db == null) return;
//        if (deletePositionList.contains(position) || )
        Expression exp = mExpList.get(position);
        int imageId = exp.getImageId();
//        Cursor query = db.query("imagedb", null, "where ? = " + imageId, new String[]{"imageid"}, null, null, null);
        Cursor cursor = db.rawQuery("select base64 from imagedb where imageid = ?", new String[]{imageId + ""});
        if (cursor.moveToFirst()) {
            String base64 = cursor.getString(cursor.getColumnIndex("base64"));
            Bitmap bitmap = BitmapUtil.stringtoBitmap(base64);
            holder.expImage.setImageBitmap(bitmap);
//            holder.expName.setText(exp.getImgName());
        }
        else {
            holder.expView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mExpList.size();
    }

}