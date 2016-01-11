package shishkoam.manager;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResponseActivity extends AppCompatActivity {
    private AndroidTreeView tView;
    private Context context = this;
    private static HashMap<TreeNode, IconTreeItem> nodes = new  HashMap<TreeNode, IconTreeItem>();
    private static HashMap<TreeNode, Integer> nodesCheckBoxes = new  HashMap<TreeNode, Integer>();
    private static HashMap<TreeNode, TreeNode> parentNodes = new  HashMap<TreeNode, TreeNode>();
    private static HashMap<TreeNode, File> nodesDirs = new  HashMap<TreeNode, File>();
    private static HashMap<TreeNode, Long> nodesSize = new  HashMap<TreeNode, Long>();
    private JSONObject object;
    ViewGroup containerView;
    TreeNode root;
    TreeNode army;
    private String TextResponse;
    AsyncHttpClient client = new AsyncHttpClient();

    private static TextView statusSizeBar;
    Bundle savedInstanceState;
    HashMap<String,String> fileList = new HashMap<String,String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        initResponse(savedInstanceState);
    }

    private void initResponse(Bundle savedInstanceState) {
        setContentView(R.layout.activity_response);
        containerView = (ViewGroup) findViewById(R.id.container);
        root = TreeNode.root();

        File directory = new File(Environment.getExternalStorageDirectory().toString() + "/Army");
        army = getFileList();
        root.addChild(army);

//        statusSizeBar = (TextView) findViewById(R.id.status_size_bar);
//        statusSizeBar.setText("Package Size: " + folderSize(directory)/1024 + "  Free Size: " + Utils.megabytesAvailableInExternalStorage() +
//                " / " + Utils.TotalExtMemory());

        tView = new AndroidTreeView(this, root);
        tView.setDefaultAnimation(true);
        tView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        tView.setDefaultNodeClickListener(nodeClickListener);
        tView.setDefaultNodeLongClickListener(nodeLongClickListener);

        containerView.addView(tView.getView());

        if (savedInstanceState != null) {
            String state = savedInstanceState.getString("tState");
            if (!TextUtils.isEmpty(state)) {
                tView.restoreState(state);
            }
        }

//        initNodesSize();
    }

    public static long getNodeSize(TreeNode node){
        return nodesSize.get(node);
    }

    private void initNodesSize() {
        for(Map.Entry<TreeNode, File> entry : nodesDirs.entrySet()) {
//            setNodeSize(entry.getKey(), entry.getValue());
            setNodeSize(entry.getKey(), folderSize(entry.getValue()));
        }
    }

    private void setNodeSize(TreeNode node, Long size) {
        IconTreeItem item = nodes.get(node);
        if (node.getChildren().size() != 0) {
            item.setSize(size/1028);
            ResponseTreeItemHolder holder = (ResponseTreeItemHolder) node.getViewHolder();
            holder.setSizeText(size/1028);
        }
    }


    private static long folderSize(File directory) {
        long length = 0;
        if (directory.exists())
            if (directory.listFiles() != null)
                for (File file : directory.listFiles()) {
                    if (file.isFile())
                        length += file.length();
                    else
                        length += folderSize(file);
                }
        return length;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.response_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.expandAll:
                tView.expandAll();
                for(Map.Entry<TreeNode, IconTreeItem> entry : nodes.entrySet()) {
                    setViewOfIconTreeItem(entry.getKey(),true);
                }
                break;

            case R.id.collapseAll:
                tView.collapseAll();
                for(Map.Entry<TreeNode, IconTreeItem> entry : nodes.entrySet()) {
                    setViewOfIconTreeItem(entry.getKey(),false);
                }
                break;
            case R.id.action_refresh:
                tView.removeNode(root);
                root.deleteChild(army);
                containerView.removeView(tView.getView());
                initResponse(savedInstanceState);
//                Intent intent = new Intent(this, ResponseActivity.class);
//                startActivity(intent);
                break;
            case R.id.action_delete:
                break;
        }
        return true;
    }

    private void setAllCheckBoxesUnchecked(){
        for(Map.Entry<TreeNode, Integer> entry : nodesCheckBoxes.entrySet()) {
            if (entry.getValue() == 0) {
                nodesCheckBoxes.put(entry.getKey(),-1);
                changeCheckBox(entry.getKey(),-1);
            }
        }
    }

    private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            changeViewOfIconTreeItem(node);
        }
    };

    private void changeViewOfIconTreeItem(TreeNode node) {
        IconTreeItem item = nodes.get(node);
        if (node.getChildren().size() != 0) {
//            item.setChecked(changeBooleanValue(item.isChecked()));
            item.setOpened(changeBooleanValue(item.isOpened()));
            ResponseTreeItemHolder holder = (ResponseTreeItemHolder) node.getViewHolder();
            holder.changeIcons(item.isOpened());
//            nodes.put(node,item);
        }
    }

    public static void setCheckedState(TreeNode treeNode, boolean state){
        int intState;
        if (state) intState = 1;
        else intState = -1;
        nodesCheckBoxes.put(treeNode, intState);
        statusSizeBar.setText("Selected Size:"+getChekedSize()+" " + "  Free Size: " + Utils.megabytesAvailableInExternalStorage() +
                " / " + Utils.TotalExtMemory());
        resetUpperCheckedStates(treeNode);
        resetDownCheckedStates(treeNode, state);
    }

    private static long getChekedSize(){
        long size = 0;
        for(Map.Entry<TreeNode, TreeNode> entry : parentNodes.entrySet()) {
            int counter = 0;
            if (nodesCheckBoxes.get(entry.getKey()) == 1) {
                for (Map.Entry<TreeNode, TreeNode> entry2 : parentNodes.entrySet()) {
                    if (entry.getKey() == entry2.getValue()) {
                        counter++;
                        break;
                    }
                }
                if (counter == 0)
                    size += nodesSize.get(entry.getKey());
            }
        }
        return size/1024;
    }

    private static void changeCheckBox(TreeNode treeNode, int intState){
        IconTreeItem item = nodes.get(treeNode);
        if (intState == 1) item.setChecked(true);
        else item.setChecked(false);

        ResponseTreeItemHolder holder = (ResponseTreeItemHolder) treeNode.getViewHolder();
        try {
            holder.changeCheckbox(intState);
        }catch (NullPointerException e){
        }
    }

    private static void resetDownCheckedStates(TreeNode treeNode, boolean state){
        int intState;
        if (state) intState = 1;
        else intState = -1;
        for(Map.Entry<TreeNode, TreeNode> entry : parentNodes.entrySet()) {
            if (entry.getValue() == treeNode){
                nodesCheckBoxes.put(entry.getKey(),intState);
                resetDownCheckedStates(entry.getKey(),state);
                changeCheckBox(entry.getKey(), intState);
            }
        }
    }

    private static void resetUpperCheckedStates(TreeNode treeNode){
        try {
            TreeNode parentTreeNode = parentNodes.get(treeNode);
            int intState = getSummedChildrenState(parentTreeNode);
            nodesCheckBoxes.put(parentTreeNode, intState);
            changeCheckBox(parentTreeNode, intState);
            resetUpperCheckedStates(parentTreeNode);
        }catch (NullPointerException e){
        }
    }

    private static int getSummedChildrenState(TreeNode treeNode){
        ArrayList<Integer> childrenStates = new ArrayList<>();
        for(Map.Entry<TreeNode, TreeNode> entry : parentNodes.entrySet()) {
            if (entry.getValue() == treeNode){
                childrenStates.add(nodesCheckBoxes.get(entry.getKey()));
            }
        }
        if (childrenStates.size() > 0) {
            int n = 1;
            int zeroState = childrenStates.get(0);
            for (int i = 1; i < childrenStates.size(); i++) {
                if (childrenStates.get(i) == zeroState)
                    n++;
            }
            if (n == childrenStates.size())
                return zeroState;
            else
                return  0;
        }
        else
            return 1;
    }

    public static int getCheckBoxValue(TreeNode node){
        return nodesCheckBoxes.get(node);
    }

    private void setViewOfIconTreeItem(TreeNode node, boolean open) {
        IconTreeItem item = nodes.get(node);
        if (node.getChildren().size() != 0) {
            item.setOpened(open);
            ResponseTreeItemHolder holder = (ResponseTreeItemHolder) node.getViewHolder();
            holder.changeIcons(item.isOpened());
//            nodes.put(node,item);
        }
    }

    private boolean changeBooleanValue(boolean booleanValue){
        if (booleanValue) return false;
        else return true;
    }

    private TreeNode.TreeNodeLongClickListener nodeLongClickListener = new TreeNode.TreeNodeLongClickListener() {
        @Override
        public boolean onLongClick(TreeNode node, Object value) {
            IconTreeItem item = (IconTreeItem) value;
            Toast.makeText(context, "Long click: " + item.getText(), Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tState", tView.getSaveState());
    }

    private TreeNode getFileList() {
        TreeNode mainPackage = new TreeNode(new IconTreeItem("null"));

        Log.i("shishkoam","android.os.Build.SERIAL: " + Build.SERIAL);
        client.get("http://109.86.155.68:8050/dr/sm/sm/getstructurejson/" + Build.SERIAL, new JsonHttpResponseHandler() {

                @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    object = response;
                }

                }

//            }

        });

        try {
            Log.d("shishkoam", getString("fileName", object) + " - " + getString("fileSize", object));
            IconTreeItem iconTreeItem = new IconTreeItem( getString("fileName", object));
            mainPackage = new TreeNode(iconTreeItem).setViewHolder(new ResponseTreeItemHolder(context));
            nodes.put(mainPackage, iconTreeItem);
            nodesCheckBoxes.put(mainPackage, -1);
            nodesSize.put(mainPackage, (long) getInt("fileSize", object));
            getListFromJson(object.getJSONArray("inDirectoryList"), mainPackage);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
            try {
//                Resources res = getResources();
//                InputStream in_s = res.openRawResource(R.raw.example);
//                byte[] b = new byte[in_s.available()];
//                in_s.read(b);
//                object = new JSONObject(new String(b));
                object = new JSONObject(getText());
                Log.d("shishkoam", getString("fileName", object) + " - " + getString("fileSize", object));
                IconTreeItem iconTreeItem = new IconTreeItem( getString("fileName", object));
                mainPackage = new TreeNode(iconTreeItem).setViewHolder(new ResponseTreeItemHolder(context));
                nodes.put(mainPackage, iconTreeItem);
                nodesCheckBoxes.put(mainPackage, -1);
                nodesSize.put(mainPackage, (long) getInt("fileSize", object));
                getListFromJson(object.getJSONArray("inDirectoryList"), mainPackage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return mainPackage;
    }

    public String getText() {
        // get connection..
        client.get("http://109.86.155.68:8050/dr/sm/sm/getstructurejson/141341414", new TextHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                TextResponse = response;
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
            }
        });
        return TextResponse;
    }

    private void getListFromJson(JSONArray jArr, TreeNode parentNode) throws JSONException{
//        JSONArray jArr = jObj.getJSONArray("list");
        for (int i=0;i<jArr.length();i++) {
            JSONObject jListObj = jArr.getJSONObject(i);
            fileList.put(getString("fileName", jListObj),getString("fileSize", jListObj));
            IconTreeItem iconTreeItem = new IconTreeItem( getString("fileName", jListObj));
            TreeNode node = new TreeNode(iconTreeItem).setViewHolder(new ResponseTreeItemHolder(context));
            nodes.put(node, iconTreeItem);
            nodesCheckBoxes.put(node, -1);
            nodesSize.put(node, (long) getInt("fileSize", jListObj));
            parentNode.addChildren(node);
            parentNodes.put(parentNode, node);
            Log.d("FileManagerClient", getString("fileName", jListObj) + " - " + getString("fileSize", jListObj));
            if (jListObj.getJSONArray("inDirectoryList").length() != 0)
                getListFromJson(jListObj.getJSONArray("inDirectoryList"),node);

        }
//        return getString("fileName", jListObj) + getString("fileSize", jListObj);
    }

    private static JSONObject getObject(String tagName, JSONObject jObj)  throws JSONException {
        JSONObject subObj = jObj.getJSONObject(tagName);
        return subObj;
    }

    private static String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }

    private static float  getFloat(String tagName, JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble(tagName);
    }

    private static int  getInt(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getInt(tagName);
    }
}
