package com.adamnickle.deck;

/**
 * Created by Adam on 8/19/2014.
 */
public final class CardResources
{
    private CardResources() { }

    public static final int BLUE_CARD_BACK = R.drawable.card_blue_back;
    public static final int RED_CARD_BACK = R.drawable.card_red_back;

    public static final int[] CARD_RESOURCE_BY_CARD_NUMBER =
    {
            // SPADES
            R.drawable.card_2s,
            R.drawable.card_3s,
            R.drawable.card_4s,
            R.drawable.card_5s,
            R.drawable.card_6s,
            R.drawable.card_7s,
            R.drawable.card_8s,
            R.drawable.card_9s,
            R.drawable.card_10s,
            R.drawable.card_js,
            R.drawable.card_qs,
            R.drawable.card_ks,
            R.drawable.card_as,

            // HEARTS
            R.drawable.card_2h,
            R.drawable.card_3h,
            R.drawable.card_4h,
            R.drawable.card_5h,
            R.drawable.card_6h,
            R.drawable.card_7h,
            R.drawable.card_8h,
            R.drawable.card_9h,
            R.drawable.card_10h,
            R.drawable.card_jh,
            R.drawable.card_qh,
            R.drawable.card_kh,
            R.drawable.card_ah,

            // CLUBS
            R.drawable.card_2c,
            R.drawable.card_3c,
            R.drawable.card_4c,
            R.drawable.card_5c,
            R.drawable.card_6c,
            R.drawable.card_7c,
            R.drawable.card_8c,
            R.drawable.card_9c,
            R.drawable.card_10c,
            R.drawable.card_jc,
            R.drawable.card_qc,
            R.drawable.card_kc,
            R.drawable.card_ac,

            // DIAMONDS
            R.drawable.card_2d,
            R.drawable.card_3d,
            R.drawable.card_4d,
            R.drawable.card_5d,
            R.drawable.card_6d,
            R.drawable.card_7d,
            R.drawable.card_8d,
            R.drawable.card_9d,
            R.drawable.card_10d,
            R.drawable.card_jd,
            R.drawable.card_qd,
            R.drawable.card_kd,
            R.drawable.card_ad,
    };

    public static final int[][] CARD_RESOURCE_BY_SUIT_RANK =
    {
        // SPADES
        {
            R.drawable.card_2s,
            R.drawable.card_3s,
            R.drawable.card_4s,
            R.drawable.card_5s,
            R.drawable.card_6s,
            R.drawable.card_7s,
            R.drawable.card_8s,
            R.drawable.card_9s,
            R.drawable.card_10s,
            R.drawable.card_js,
            R.drawable.card_qs,
            R.drawable.card_ks,
            R.drawable.card_as
        },

        // HEARTS
        {
            R.drawable.card_2h,
            R.drawable.card_3h,
            R.drawable.card_4h,
            R.drawable.card_5h,
            R.drawable.card_6h,
            R.drawable.card_7h,
            R.drawable.card_8h,
            R.drawable.card_9h,
            R.drawable.card_10h,
            R.drawable.card_jh,
            R.drawable.card_qh,
            R.drawable.card_kh,
            R.drawable.card_ah,
        },

        // CLUBS
        {
            R.drawable.card_2c,
            R.drawable.card_3c,
            R.drawable.card_4c,
            R.drawable.card_5c,
            R.drawable.card_6c,
            R.drawable.card_7c,
            R.drawable.card_8c,
            R.drawable.card_9c,
            R.drawable.card_10c,
            R.drawable.card_jc,
            R.drawable.card_qc,
            R.drawable.card_kc,
            R.drawable.card_ac,
        },

        // DIAMONDS
        {
            R.drawable.card_2d,
            R.drawable.card_3d,
            R.drawable.card_4d,
            R.drawable.card_5d,
            R.drawable.card_6d,
            R.drawable.card_7d,
            R.drawable.card_8d,
            R.drawable.card_9d,
            R.drawable.card_10d,
            R.drawable.card_jd,
            R.drawable.card_qd,
            R.drawable.card_kd,
            R.drawable.card_ad,
        }
    };
}
