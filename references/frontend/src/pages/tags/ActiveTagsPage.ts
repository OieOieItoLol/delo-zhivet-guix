import { apiCall, css, l, lz, sync, type AsyncClassComponent, type ClassComponent } from "../../lib"
import { tagColor } from "../../tag";
import { stButton, stButtonOutline } from "../../uikit/Button";
import { stCheckbox } from "../../uikit/Checkbox";
import { stInput } from "../../uikit/Input";
import { Modal } from "../../uikit/Modal";
import { type TagData, type TaskType } from './Types';


class TagEdit implements ClassComponent<HTMLDivElement> {

    constructor(
        readonly tag: TagData,
        readonly isBotMode: boolean,
        readonly archiveCallback: () => void,
        readonly botHiddenFlipCallback: () => void,
    ) { }

    mount(): HTMLDivElement {
        return l('div', _ => {
            css`
                font-size: 14px;
                border-radius: 30px;
                padding: 3px 10px;
                color: white;
                font-weight: bold;
                display: flex;
                align-items: center;
                gap: 6px;
            `.apply(_)

            _.style.backgroundColor = tagColor(this.tag.name)

            l(_, 'span', _ => { _.innerText = this.tag.name })

            l(_, 'span', async _ => {
                css`
                    cursor: pointer;
                    display: inline-block;
                    width: 9px;
                    height: 9px;
                    background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" viewBox="0 0 10 10" fill="none"><path d="M8.71422 1.28564L1.28564 8.71422" stroke="white" stroke-linecap="round" stroke-linejoin="round"/><path d="M1.28564 1.28564L8.71422 8.71422" stroke="white" stroke-linecap="round" stroke-linejoin="round"/></svg>');
                `.apply(_)
                _.title = 'Сделать неактивным'
                _.onclick = async () => this.archiveCallback()
            })

            if (this.isBotMode) {
                if (!this.tag.isHiddenInBot)
                    l(_, 'span', _ => {
                        css`
                        cursor: pointer;
                        display: inline-block;
                        width: 13px;
                        height: 12px;
                        background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="14" height="12" viewBox="0 0 14 12" fill="none"><path d="M2.43945 3.60547C3.27364 5.15576 5.00269 6.22079 7.00039 6.22079C8.99809 6.22079 10.7271 5.15576 11.5613 3.60547" stroke="white" stroke-linecap="round" stroke-linejoin="round"/><path d="M3.35648 4.79688L1.42871 6.41248" stroke="white" stroke-linecap="round" stroke-linejoin="round"/><path d="M5.64259 6.05688L4.71436 8.39459" stroke="white" stroke-linecap="round" stroke-linejoin="round"/><path d="M10.6436 4.79688L12.5714 6.41248" stroke="white" stroke-linecap="round" stroke-linejoin="round"/><path d="M8.35449 6.05688L9.28273 8.39459" stroke="white" stroke-linecap="round" stroke-linejoin="round"/></svg>');
                    `.apply(_)
                        _.title = 'Запретить в боте'
                        _.onclick = async () => this.botHiddenFlipCallback()
                    })
            } else {
                if (this.tag.isHiddenInBot)
                    l(_, 'span', _ => {
                        css`
                        cursor: pointer;
                        display: inline-block;
                        width: 13px;
                        height: 12px;
                        background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="14" height="12" viewBox="0 0 14 12" fill="none"><path d="M12.3401 5.35394C12.4822 5.53113 12.5609 5.76133 12.5609 6.00002C12.5609 6.23871 12.4822 6.46891 12.3401 6.6461C11.4401 7.73576 9.39147 9.85719 7.00004 9.85719C4.60862 9.85719 2.56004 7.73576 1.66004 6.6461C1.51789 6.46891 1.43921 6.23871 1.43921 6.00002C1.43921 5.76133 1.51789 5.53113 1.66004 5.35394C2.56004 4.26428 4.60862 2.14282 7.00004 2.14282C9.39147 2.14282 11.4401 4.26428 12.3401 5.35394Z" stroke="white" stroke-linecap="round" stroke-linejoin="round"/><path d="M7.00017 7.71422C7.94695 7.71422 8.71446 6.9467 8.71446 5.99993C8.71446 5.05316 7.94695 4.28564 7.00017 4.28564C6.0534 4.28564 5.28589 5.05316 5.28589 5.99993C5.28589 6.9467 6.0534 7.71422 7.00017 7.71422Z" stroke="white" stroke-linecap="round" stroke-linejoin="round"/></svg>');
                    `.apply(_)
                        _.title = 'Разрешить в боте'
                        _.onclick = async () => this.botHiddenFlipCallback()
                    })
            }
        })
    }
}

export class ActiveTagsPage implements AsyncClassComponent<HTMLDivElement> {

    async mount(): Promise<HTMLDivElement> {

        let tags: Array<TagData> = await apiCall<Array<TagData>>('/manager/tag/usersTags', true)

        return lz('div', (_, zTags) => {
            css`
                display: flex;
                flex-direction: row;
                flex-wrap: wrap;
                gap: 32px;

                &>div {
                    min-width: 400px;
                    max-width: calc(50% - 16px);
                    display: flex;
                    flex-direction: column;
                    gap: 24px;
                }
            `.apply(_)

            const render = (isBotMode: boolean) => {
                type TaskTypeView = {
                    t: TaskType,
                    name: string
                }

                const types: Array<TaskTypeView> = [
                    { t: 'Ecological', name: 'Экологические' },
                    { t: 'Social', name: 'Социальные' }
                ]

                l(_, 'div', _ => {
                    l(_, 'span', _ => {
                        css`
                            font-size: 24px;
                            font-weight: bold;
                        `.apply(_)
                        _.innerText = isBotMode ? 'В боте' : 'На сайте'
                    })

                    for (let t of types) {
                        l(_, 'span', _ => {
                            css`
                                color: #F87244;
                                font-size: 20px;
                            `.apply(_)
                            _.innerText = t.name
                        })

                        l(_, 'div', _ => {
                            css`
                                display: flex;
                                flex-wrap: wrap;
                                gap: 16px;
                            `.apply(_)

                            for (let currTag of tags) {
                                if (
                                    (currTag.isHiddenInBot && isBotMode) || currTag.taskType != t.t
                                ) continue

                                l(_, new TagEdit(currTag, isBotMode,
                                    async () => sync([zTags],
                                        [await apiCall('/manager/tag/changeTagArchiveStatus', { tagId: currTag.id, isArchived: true }),
                                        tags = tags.filter(tag => tag.id != currTag.id)]),
                                    async () => sync([zTags],
                                        [await apiCall('/manager/tag/changeTagBotAvailability', { tagId: currTag.id, isHidden: !currTag.isHiddenInBot }),
                                        currTag.isHiddenInBot = !currTag.isHiddenInBot])
                                )
                                )
                            }

                            if (!isBotMode)
                                l(_, new Modal(
                                    onModalFinish => l('div', _ => {
                                        css`
                                            background-color: white;
                                            display: flex;
                                            flex-direction: column;
                                            align-items: center;
                                            justify-content: center;
                                            border-radius: 30px;
                                            padding: 24px 32px;
                                            gap: 16px;
                                        `.apply(_)

                                        l(_, 'span', _ => {
                                            css`
                                                font-size: 18px;
                                                font-weight: 600;
                                            `.apply(_)
                                            _.innerText = 'Создание тэга'
                                        })

                                        let isHiddenInBot = true
                                        let newTagName = ''

                                        l(_, 'div', _ => {
                                            stInput.apply(_)

                                            l(_, 'span', _ => {
                                                css`     
                                                    width: 24px !important;
                                                    height: 22px !important;                          
                                                    background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 22 22" fill="none"><path d="M7.92531 19.0617L1 21L2.93909 14.0774L15.6201 1.46285C15.7634 1.31633 15.9347 1.19991 16.1235 1.12043C16.3125 1.04094 16.5155 1 16.7205 1C16.9255 1 17.1284 1.04094 17.3174 1.12043C17.5063 1.19991 17.6775 1.31633 17.8208 1.46285L20.5448 4.20113C20.689 4.34414 20.8035 4.5143 20.8817 4.70175C20.9598 4.88921 21 5.09029 21 5.29337C21 5.49645 20.9598 5.69753 20.8817 5.88499C20.8035 6.07246 20.689 6.2426 20.5448 6.38561L7.92531 19.0617Z" stroke="black" stroke-linecap="round" stroke-linejoin="round"/></svg>');
                                                `.apply(_)
                                            })
                                            l(_, 'input', _ => {
                                                _.size = 30
                                                _.placeholder = 'Название тэга'
                                                _.onchange = __ => newTagName = _.value
                                            })
                                        })
                                        l(_, 'div', _ => {
                                            css`
                                                display: flex;
                                                align-items: center;
                                            `.apply(_)
                                            l(_, 'input', _ => {
                                                _.type = 'checkbox'
                                                stCheckbox.apply(_)
                                                _.onchange = __ => isHiddenInBot = !_.checked
                                                    
                                            })
                                            l(_, 'span', _ => { _.innerText = 'Показывать в боте' })
                                        })

                                        const doJob = async () => {
                                            const name = newTagName
                                            const id = await apiCall<number>('/manager/tag/add', { name: name, taskType: t.t, isHiddenInBot: isHiddenInBot })
                                            tags.push({ id: id, name: name, taskType: t.t, isHiddenInBot: isHiddenInBot, isArchived: false })

                                            onModalFinish()
                                            sync([zTags], [])
                                        }

                                        l(_, 'div', _ => {
                                            css`
                                                display: flex;
                                                gap: 10px;
                                            `.apply(_)

                                            l(_, 'button', _ => {
                                                stButtonOutline.apply(_)
                                                _.innerText = 'Отмена'
                                                _.onclick = __ => onModalFinish()
                                            })
                                            l(_, 'button', _ => {
                                                stButton.apply(_)
                                                _.innerText = 'Создать тэг'
                                                _.onclick = __ => doJob()
                                            })
                                        })
                                    }),
                                    l('span', _ => {

                                        css`
                                            cursor: pointer;
                                            display: inline-block;
                                            &::before {
                                                content: "";
                                                display: inline-block;
                                                width: 10px;
                                                height: 10px;
                                                margin-right: 6px;
                                                background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" viewBox="0 0 10 10" fill="none"><g clip-path="url(%23clip0_816_1165)"><path d="M5 0.357178V9.64289" stroke="black" stroke-linecap="round" stroke-linejoin="round"/><path d="M0.356934 4.97144H9.64265" stroke="black" stroke-linecap="round" stroke-linejoin="round"/></g><defs><clipPath id="clip0_816_1165"><rect width="10" height="10" fill="white"/></clipPath></defs></svg>');
                                            }
                                        `.apply(_)
                                        _.innerText = 'Добавить тег'
                                    })
                                ))
                        })
                    }
                })
            }

            render(false)
            render(true)
        })
    }
}