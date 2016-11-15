package com.example.kelly.myapplication;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;


public class GameActivity extends Activity {
    private ImageView[][] cells;
    private View boardView;
    private Board board;
    private ViewHolder viewHolder;
    private static int NUM_ROWS = 6;
    private static int NUM_COLS = 7;
    private ImageView turnView;
    private TextView turnTextView;

    private MediaPlayer bg_player;

    @Override
    protected void onStart(){
        super.onStart();
        bg_player = MediaPlayer.create(this, R.raw.abc);
        bg_player.start();
    }

    @Override
    protected void onStop(){
        super.onStop();
        bg_player.stop();
    }



    //struct for recall
    private class RecCell
    {
        public int row;
        public int col;
        public Board.Turn turn;//only for recall the recall.
    }

    ArrayList<RecCell> recordList;

    private class ViewHolder {
        public TextView winTextView;
        public ImageView winImageView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        board = new Board(NUM_ROWS, NUM_COLS);
        boardView = findViewById(R.id.game_board);
        turnView = (ImageView)findViewById(R.id.turn_view);
        turnView.setImageResource(resourceForTurn());
        turnView.setVisibility(View.VISIBLE);

        turnTextView = (TextView)findViewById(R.id.turnTextView);
        turnTextView.setVisibility(View.VISIBLE);


        recordList = new ArrayList<RecCell>();

        buildCells();
        boardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP: {
                        int col = colAtX(motionEvent.getX());//点击X坐标
                        if (col != -1)
                            drop(col);
                    }
                }
                return true;
            }
        });
        Button resetButton = (Button) findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });

        //recall button
        Button recallBtn = (Button) findViewById(R.id.recall_button);
        recallBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                recall();
            }
        });

        viewHolder = new ViewHolder();
        viewHolder.winTextView = (TextView)findViewById(R.id.win_text_view);
        viewHolder.winImageView = (ImageView) findViewById(R.id.win_image_view);
        viewHolder.winImageView.setImageResource(resourceForTurn());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }//delete

    private void buildCells() {//初始化,映射
        cells = new ImageView[NUM_ROWS][NUM_COLS];
        for (int r=0; r<NUM_ROWS; r++) {
            ViewGroup row = (ViewGroup) ((ViewGroup) boardView).getChildAt(r);//获取是哪一行
            row.setClipChildren(false);
            for (int c=0; c<NUM_COLS; c++) {//获取每一列
                ImageView imageView = (ImageView) row.getChildAt(c);
                imageView.setImageResource(android.R.color.transparent);
                cells[r][c] = imageView;//关联XML文件
            }
        }
    }

    private void drop(int col) {
        if (board.hasWinner)//胜负已分
            return;
        int row = board.lastAvailableRow(col);//点上面,棋子落下面
        if (row == -1)
            return;
        final ImageView cell = cells[row][col];
        float move = -(cell.getHeight() * row + cell.getHeight() + 15);
        cell.setY(move);
        cell.setImageResource(resourceForTurn());
        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, Math.abs(move));
       //anim.setDuration(150);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                return;//no action at present
            }

            @Override
            public void onAnimationEnd(Animation animation){
                if (board.checkForWin()) {
                    Integer Row, Col;
                    for (int i = 0; i < board.winCellSiteSet.size(); ++i){
                        for (int j = 0; j < board.winCellSiteSet.get(i).size(); ++j){
                            Row = board.winCellSiteSet.get(i).get(j).row;
                            Col = board.winCellSiteSet.get(i).get(j).col;
                            String str = "(".toString() + Row.toString() + ",".toString() + Col.toString() + ")".toString();
                            Log.i("site".toString(), str);
                        }
                        Log.i("site".toString(), "====================".toString());
                    }

                    win();
                } else {
                    //judge for draw(tie)
                    int ic;
                    for (ic = 0; ic < NUM_COLS; ++ic){//123
                        if (board.cells[0][ic].empty){//010203第一行,第一行被占满
                            break;
                        }
                    }
                    if (NUM_COLS == ic){  //tie
                        viewHolder.winTextView.setText("Draw!".toString());
                        viewHolder.winTextView.setVisibility(TextView.VISIBLE);
                        turnView.setVisibility(View.INVISIBLE);
                        turnTextView.setVisibility(View.INVISIBLE);
                        board.hasWinner = true;       //hasDraw = true??
                        Log.i("Info".toString(), "Tie".toString());
                        return ;
                    }
                    changeTurn();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                return ;//no action at present
            }
        });
        anim.setFillAfter(true);
        cell.startAnimation(anim);
        board.occupyCell(row, col);//逻辑上放棋子

        //store the step to record.
        RecCell recCell = new RecCell();
        recCell.row = row;
        recCell.col = col;
        recCell.turn = board.turn;
        recordList.add(recCell);//放到list里面,到时候再从list里面去去
    }

    private void win() {
        //-test print the test info.
        Log.i("Test Info".toString(), board.testStr);

        //modify the win cell//改变棋子颜色
        int res = board.turn == Board.Turn.FIRST ? (R.drawable.rw) : (R.drawable.gw);
        for (int i = 0; i < board.winCellSiteSet.size(); ++i){
            for (int j = 0; j < board.winCellSiteSet.get(i).size(); ++j){
                ImageView cell = cells[board.winCellSiteSet.get(i).get(j).row][board.winCellSiteSet.get(i).get(j).col];
                cell.setImageResource(res);//获取赢的子,然后再把颜色改变
            }
        }

        //show
        viewHolder.winTextView.setText("win:".toString());
        viewHolder.winTextView.setVisibility(TextView.VISIBLE);
        viewHolder.winImageView.setImageResource(resourceForTurn());

        viewHolder.winImageView.setVisibility(TextView.VISIBLE);
        turnView.setVisibility(View.INVISIBLE);
        turnTextView.setVisibility(View.INVISIBLE);


    }

    private void changeTurn() {

        board.toggleTurn();
        turnView.setImageResource(resourceForTurn());
    }

    //不精确
    private int colAtX(float x) {//真正坐标得知,把Fola转化为COL,X点击的坐标
        float colWidth = cells[0][0].getWidth();
        int col = (int) (x - 30) / (int) colWidth;
        if (col < 0 || col > 6)
            return -1;
        return col;
    }

    private int resourceForTurn() {
        switch (board.turn) {
            case FIRST:
                return R.drawable.red;
            case SECOND:
                return R.drawable.green;
        }
        return R.drawable.red;
    }

    private void reset() {
        board.reset();
        for (int r=0; r<NUM_ROWS; r++) {
            for (int c=0; c<NUM_COLS; c++) {
                cells[r][c].setImageResource(android.R.color.transparent);
            }
        }

        viewHolder.winTextView.setVisibility(TextView.INVISIBLE);
        viewHolder.winImageView.setVisibility(TextView.INVISIBLE);
        turnView.setVisibility(View.VISIBLE);
        turnTextView.setVisibility(View.VISIBLE);
    }

    //recall proc
    private void recall() {
        if (recordList.isEmpty())
        {
            return;
        }
        //clear the result.
        if (board.hasWinner)
        {
            //modify the win cell back.
            for (int i = 0; i < board.winCellSiteSet.size(); ++i){
                for (int j = 0; j < board.winCellSiteSet.get(i).size(); ++j){
                    ImageView cell = cells[board.winCellSiteSet.get(i).get(j).row][board.winCellSiteSet.get(i).get(j).col];
                    cell.setImageResource(resourceForTurn());
                }
            }

            board.hasWinner = false;

            viewHolder.winTextView.setVisibility(TextView.INVISIBLE);

            viewHolder.winImageView.setVisibility(TextView.INVISIBLE);

      /*cause not change player if someone win.
      so,recall this type should change player twice.
       */
            changeTurn();
        }
        turnView.setVisibility(View.VISIBLE);
        turnTextView.setVisibility(View.VISIBLE);
        int indexLast = recordList.size() - 1;
        RecCell recCell = recordList.get(indexLast);
        board.clearCell(recCell.row, recCell.col);
        //cells[recCell.row][recCell.col].setImageResource(android.R.color.transparent);

        ViewGroup row = (ViewGroup) ((ViewGroup) boardView).getChildAt(recCell.row);//数组里面获取行
        row.setClipChildren(false);
        ImageView imageView = (ImageView) row.getChildAt(recCell.col);
        imageView.setImageResource(android.R.color.transparent);
        cells[recCell.row][recCell.col] = imageView;

        //delete the record being recalled.
        recordList.remove(indexLast);

        //changed the player
        changeTurn();

    }

}