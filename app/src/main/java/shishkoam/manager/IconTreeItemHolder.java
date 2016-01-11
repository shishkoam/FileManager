package shishkoam.manager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

/**
 * Created by ав on 25.12.2015.
 */
public class IconTreeItemHolder extends TreeNode.BaseNodeViewHolder<IconTreeItem> {
    private TextView tvValue;
    private ImageView arrowView;
    private ImageView packageView;
    private TextView sizeText;
    private CheckBox checkBox;

    public IconTreeItemHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(final TreeNode node, IconTreeItem value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_icon_node, null, false);
        tvValue = (TextView) view.findViewById(R.id.node_value);
        tvValue.setText(value.getText());

        arrowView = (ImageView) view.findViewById(R.id.arrow_icon);
        packageView = (ImageView) view.findViewById(R.id.icon);
        sizeText = (TextView) view.findViewById(R.id.size);
        checkBox = (CheckBox) view.findViewById(R.id.checkbox);

        if (node.getChildren().size() == 0)
            arrowView.setVisibility(View.INVISIBLE);

        sizeText.setText(" " + FileManagerActivity.getNodeSize(node)/1024 + " kb ");
            final int status = FileManagerActivity.getCheckBoxValue(node);
            changeCheckbox(status);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileManagerActivity.setCheckedState(node, checkBox.isChecked());
                if (checkBox.isChecked()) {
                    checkBox.setButtonDrawable(R.drawable.ic_checkbox_marked);
                    checkBox.setChecked(true);
                }
                else {
                    checkBox.setButtonDrawable(R.drawable.ic_checkbox_blank_outline);
                    checkBox.setChecked(false);
                }
                FileManagerActivity.setCheckedState(node, checkBox.isChecked());
            }
        });


        if (node.getLevel() == 1) {
            checkBox.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    public void setSizeText(long size){
        try {
            sizeText.setText(" " + (int)size + " kb ");
        }
        catch(NullPointerException e){
//            sizeText.setText(" " + 0 + " kb ");
        }
    }

    public void changeIcons(boolean value){
        if (value){
            Drawable arrowPoped = context.getResources().getDrawable( R.drawable.ic_chevron_down );
            arrowView.setImageDrawable(arrowPoped);
            Drawable packagePoped = context.getResources().getDrawable( R.drawable.ic_package_variant );
            packageView.setImageDrawable(packagePoped);
        }
        else{
            Drawable arrowUnPoped = context.getResources().getDrawable( R.drawable.ic_chevron_right );
            arrowView.setImageDrawable(arrowUnPoped);
            Drawable packageUnPoped = context.getResources().getDrawable( R.drawable.ic_package_variant_closed );
            packageView.setImageDrawable(packageUnPoped);
        }
    }

    public void changeCheckbox(int status) {
        if (status == 1) {
            checkBox.setChecked(true);
            checkBox.setButtonDrawable(R.drawable.ic_checkbox_marked);
        }
        else {
            checkBox.setChecked(false);
            checkBox.setButtonDrawable(R.drawable.ic_checkbox_blank_outline);
        }
        if (status == 0) {
            checkBox.setButtonDrawable(R.drawable.ic_checkbox_blank);
        }
    }

    @Override
    public void toggle(boolean active) {
    }

}