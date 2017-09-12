package com.example.circlepath_library;

/**
 *
 * Created by LiuLei on 2017/9/12.
 */

public interface CircleMenuLisenter {

    /**
     * Called when menu opened
     */
    void menuOpen();

    /**
     * Called when menu Closed
     */
    void menuClose();

    /**
     * Called when Menu item Clicked
     *
     * @param menuNumber give menu number which clicked.
     */
    void menuItemClicked(int menuNumber);

    void isMove();

}
