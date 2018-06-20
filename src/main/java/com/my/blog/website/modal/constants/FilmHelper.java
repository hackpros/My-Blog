package com.my.blog.website.modal.constants;

/**
 * <p>Description: 实现dao中的方法</p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: jumore</p>
 *
 * @author rongzheng
 * @date 2018/6/11
 */
public interface FilmHelper {

    enum EStatus {

        /**
         *
         */
        NO_TRANS(1),

        /**
         *
         */
        UN_TRANS(3),

        /**
         *
         */
        HAS_TRANS(5);

        private int status;

        public int getStatus() {
            return status;
        }

        EStatus(int status) {
            this.status=status;
        }
    }
}
