import { css } from "../lib";

export const stFormLayout = css`
    display: flex;
    gap: 8px;
    flex-direction: column;
    
    &>div {
        display: flex;
        align-items: center;

        &>span:nth-child(1) {
            max-width: 130px;
            min-width: 130px;
            font-weight: 600;
            color: rgba(0, 0, 0, 0.25);
            line-height: 40px;

            &::before {
                content: '';
                position: relative;
                width: 30px;
                height: 20px;
                top: 3px;
                display: inline-block;
            }
        }
    }
`