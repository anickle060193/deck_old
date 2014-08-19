package com.adamnickle.deck;

/**
 * Created by Adam on 8/19/2014.
 */
public final class CardResources
{
    private CardResources() { }

    public static final int BLUE_CARD_BACK = R.raw.card_blue_back;
    public static final int RED_CARD_BACK = R.raw.card_red_back;

    public static final int[] CARD_RESOURCE_BY_CARD_NUMBER =
    {
            // SPADES
            R.raw.card_2s,
            R.raw.card_3s,
            R.raw.card_4s,
            R.raw.card_5s,
            R.raw.card_6s,
            R.raw.card_7s,
            R.raw.card_8s,
            R.raw.card_9s,
            R.raw.card_10s,
            R.raw.card_js,
            R.raw.card_qs,
            R.raw.card_ks,
            R.raw.card_as,

            // HEARTS
            R.raw.card_2h,
            R.raw.card_3h,
            R.raw.card_4h,
            R.raw.card_5h,
            R.raw.card_6h,
            R.raw.card_7h,
            R.raw.card_8h,
            R.raw.card_9h,
            R.raw.card_10h,
            R.raw.card_jh,
            R.raw.card_qh,
            R.raw.card_kh,
            R.raw.card_ah,

            // CLUBS
            R.raw.card_2c,
            R.raw.card_3c,
            R.raw.card_4c,
            R.raw.card_5c,
            R.raw.card_6c,
            R.raw.card_7c,
            R.raw.card_8c,
            R.raw.card_9c,
            R.raw.card_10c,
            R.raw.card_jc,
            R.raw.card_qc,
            R.raw.card_kc,
            R.raw.card_ac,

            // DIAMONDS
            R.raw.card_2d,
            R.raw.card_3d,
            R.raw.card_4d,
            R.raw.card_5d,
            R.raw.card_6d,
            R.raw.card_7d,
            R.raw.card_8d,
            R.raw.card_9d,
            R.raw.card_10d,
            R.raw.card_jd,
            R.raw.card_qd,
            R.raw.card_kd,
            R.raw.card_ad,
    };

    public static final int[][] CARD_RESOURCE_BY_SUIT_RANK =
    {
        // SPADES
        {
            R.raw.card_2s,
            R.raw.card_3s,
            R.raw.card_4s,
            R.raw.card_5s,
            R.raw.card_6s,
            R.raw.card_7s,
            R.raw.card_8s,
            R.raw.card_9s,
            R.raw.card_10s,
            R.raw.card_js,
            R.raw.card_qs,
            R.raw.card_ks,
            R.raw.card_as
        },

        // HEARTS
        {
            R.raw.card_2h,
            R.raw.card_3h,
            R.raw.card_4h,
            R.raw.card_5h,
            R.raw.card_6h,
            R.raw.card_7h,
            R.raw.card_8h,
            R.raw.card_9h,
            R.raw.card_10h,
            R.raw.card_jh,
            R.raw.card_qh,
            R.raw.card_kh,
            R.raw.card_ah,
        },

        // CLUBS
        {
            R.raw.card_2c,
            R.raw.card_3c,
            R.raw.card_4c,
            R.raw.card_5c,
            R.raw.card_6c,
            R.raw.card_7c,
            R.raw.card_8c,
            R.raw.card_9c,
            R.raw.card_10c,
            R.raw.card_jc,
            R.raw.card_qc,
            R.raw.card_kc,
            R.raw.card_ac,
        },

        // DIAMONDS
        {
            R.raw.card_2d,
            R.raw.card_3d,
            R.raw.card_4d,
            R.raw.card_5d,
            R.raw.card_6d,
            R.raw.card_7d,
            R.raw.card_8d,
            R.raw.card_9d,
            R.raw.card_10d,
            R.raw.card_jd,
            R.raw.card_qd,
            R.raw.card_kd,
            R.raw.card_ad,
        }
    };
}
