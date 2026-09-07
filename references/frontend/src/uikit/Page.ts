import { css } from "../lib";

export const stPageHeading = css`
    font-size: 40px;
    font-weight: 600;
    outline: none;
`
export const stBackButton = css`
    background: url("/public/icons/back.svg") no-repeat 0px 0px transparent;
    min-width: 29px;
    max-width: 29px;
    height: 25px;
    display: inline-block;
    cursor: pointer;
    outline: none;
    border: none;
`

export const stFormAndMapLayout = css`
    display: grid;
    grid-template-columns: 50% 50%;
    grid-auto-rows: auto;
`