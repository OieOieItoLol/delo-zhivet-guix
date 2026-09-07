import { apiCall, css, l, lz, sync, type AsyncClassComponent, type ClassComponent } from "../../lib";
import { tagColor } from "../../tag";
import type { TagData } from "./Types";

class TagEdit implements ClassComponent<HTMLDivElement> {

    constructor(
        readonly tag: TagData,
        readonly activateTagCallback: () => void,
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

            l(_, 'span', _ => {
                css`
                    cursor: pointer;
                    display: inline-block;
                    width: 9px;
                    height: 9px;
                    background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" viewBox="0 0 10 10" fill="none"><path d="M8.71422 1.28564L1.28564 8.71422" stroke="white" stroke-linecap="round" stroke-linejoin="round"/><path d="M1.28564 1.28564L8.71422 8.71422" stroke="white" stroke-linecap="round" stroke-linejoin="round"/></svg>');
                `.apply(_)
                _.title = 'Сделать активным'
                _.onclick = async () => this.activateTagCallback()
            })
        })
    }
}

export class InactiveTagsPage implements AsyncClassComponent<HTMLDivElement> {

    async mount(): Promise<HTMLDivElement> {

        let tags: Array<TagData> = await apiCall<Array<TagData>>('/manager/tag/usersTags', false)
        
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
            const types = [
                { t: 'Ecological', name: 'Экологические' },
                { t: 'Social', name: 'Социальные' }
            ]

                for (let t of types) {
                    l(_, 'div', _ => {
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

                            for(let currTag of tags) {
                                if(currTag.taskType != t.t) continue

                                l(_, new TagEdit(currTag, 
                                        async () => sync([zTags], 
                                            [await apiCall('/manager/tag/changeTagArchiveStatus', {tagId: currTag.id, isArchived: false}),
                                            tags = tags.filter(tag => tag.id != currTag.id)])
                                    )
                                )
                            }
                        })
                    })
                }
            }
        )
    }
}