import { css } from "../lib";

export const stInput = css`
    width: 100%;
    height: 40px;
    border-radius: 30px;
    border: 1px solid var(--base-border-color);
    display: flex;
    align-items: center;
    gap: 8px;
    padding-left: 16px;
    padding-right: 16px;

    &>span {
        display: inline-block;
        width: 22px;
        height: 20px;
    }

    &>input {
        border: none;
        outline: none;
        font-size: 16px;
        width: 100%;

        &::placeholder {
            color: #00000040;
        }
    }
`