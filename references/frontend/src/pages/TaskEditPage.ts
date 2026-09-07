import { apiCall, css, l, lz, sync, debounce, type AsyncClassComponent, type ClassComponent } from "../lib";
import L from 'leaflet'
import 'leaflet/dist/leaflet.css';
import { Dropdown } from "../uikit/Dropdown";
import { stBackButton, stFormAndMapLayout, stPageHeading } from "../uikit/Page";
import { stFormLayout } from "../uikit/Form";
import { TextArea } from "../uikit/TextArea";
import { Select } from "../uikit/Select";
import type { Link } from "../lib/router";
import { tagColor } from "../tag";
import { ConfirmModal, Modal } from "../uikit/Modal";
import { stButton, stButtonOutline } from "../uikit/Button";
import { stCheckbox } from "../uikit/Checkbox";
import { TaskStatusView, type TaskStatus } from "../uikit/TaskStatus";
import type { TaskType } from "./tags/Types";

type Volunteer = { id: number, tgName: string }
type Leader = { id: number, tgName: string }

type Photo = {
    photoId: number,
    optimizedPhotoPath: number,
    photoPath: number,
}

type Task = {
    id: number,
    name: string,
    taskType: TaskType,
    longitude: number,
    latitude: number,
    createDate: string,
    note: string,
    status: TaskStatus,
    statusDaysCount: number,
    leaderId: number,
    leaderTgName: string,
    volunteers: Array<Volunteer>,
    chatLink: string,
    tags: Array<Tag>,
    photos: Array<Photo>,
}

type Tag = {
    id: number, name: string
}

class TagEdit implements ClassComponent<HTMLDivElement> {

    constructor(
        readonly tag: Tag,
        readonly onDelete: (tag: Tag) => void,
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
                _.title = 'Удалить'
                _.onclick = async () => this.onDelete(this.tag)
            })
        })
    }
}

class TagAdd implements ClassComponent<HTMLDivElement> {

    constructor(
        readonly tag: Tag,
        readonly onToggle: (tag: Tag, isSelected: boolean) => void,
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

            l(_, 'input', _ => {
                _.type = 'checkbox'
                stCheckbox.apply(_)

                _.onchange = ev =>
                    this.onToggle(this.tag, (ev.target as HTMLInputElement).checked)
            })
        })
    }
}

export class TaskEditPage implements AsyncClassComponent<HTMLDivElement> {

    constructor(
        readonly taskId: number,
        readonly volonteerEditLink: Link<number>,
    ) { }

    mount(): Promise<HTMLDivElement> {

        return l('div', async _ => {

            const task = await apiCall<Task>('/manager/task/get', this.taskId)
            const allLeaders = await apiCall<(Leader | null)[]>('/manager/task/allLeaders', undefined)
            allLeaders.push(null) // сюда добавляем вариант убрать лидера, легально в связи с бизнес логикой

            l(_, 'div', _ => {
                css`
                    display: flex;
                    align-items: baseline;
                `.apply(_)

                l(_, 'button', _ => {
                    stBackButton.apply(_)
                    _.onclick = () => { history.back() }
                })
                l(_, 'span', _ => {
                    stPageHeading.apply(_)
                    css`
                        &::after {
                            content: "";
                            background: url("/public/icons/pencil-large.svg") no-repeat 14px 0px transparent;
                            display: inline-block;
                            width: 39px;
                            height: 25px;
                        }
                    `.apply(_)
                    _.innerText = task.name
                    _.contentEditable = 'true'
                    const titleNode = _

                    _.addEventListener('input', debounce(() => {
                        //editApiCall(async () => {
                        apiCall('/manager/task/editName',
                            { 'id': task.id, 'name': titleNode.innerText })
                        //})
                    }))
                })
            })
            l(_, 'div', _ => {
                css`
                    padding: 24px;
                    display: flex;
                    flex-direction: column;
                    gap: 32px;
                `.apply(_)

                l(_, 'div', _ => {
                    stFormAndMapLayout.apply(_)

                    l(_, 'div', _ => {
                        stFormLayout.apply(_)

                        l(_, 'div', _ => {
                            l(_, 'span', _ => {
                                css`
                                    &::before {
                                        background: url("/public/icons/task-id.svg") no-repeat 0px 0px transparent;
                                    }
                                `.apply(_)
                                _.innerText = 'ID:'
                            })
                            l(_, 'span', _ => { _.innerText = '#' + task.id })
                        })
                        l(_, 'div', _ => {
                            l(_, 'span', _ => {
                                css`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
                                    &::before {
                                        height: 24px !important;
                                        top: 5px !important;
                                        background: url("/public/icons/task-status.svg") no-repeat 0px 0px transparent;
                                    }
                                `.apply(_)
                                _.innerText = 'Статус:'
                            })
                            l(_, 'div', _ => {
                                css`
                                    display: flex;
                                    align-items: baseline;
                                `.apply(_)

                                l(_, new Select<TaskStatus>(
                                    task.status,
                                    ['New', 'InPlan', 'InProcessing', 'Done', 'Inactive', 'Archive'],
                                    ts => new TaskStatusView(ts),
                                    ts => apiCall('/manager/task/editStatus', { 'id': task.id, 'status': ts })
                                ))
                                l(_, 'span', _ => {
                                    css`
                                        font-size: 12px;
                                        margin-left: 12px;

                                        &::before {
                                            content: '';
                                            display: inline-block;
                                            width: 13px;
                                            height: 13px;
                                            background: url("/public/icons/clock.svg") no-repeat 0px 0px transparent;
                                            margin-right: 4px;
                                            position: relative;
                                            top: 1px
                                        }
                                    `.apply(_)
                                    _.innerText = `В статусе ${task.statusDaysCount} дня`
                                })
                            })
                        })
                        l(_, 'div', _ => {
                            l(_, 'span', _ => {
                                css`
                                    &::before {
                                        background: url("/public/icons/task-date.svg") no-repeat 0px 0px transparent;
                                    }
                                `.apply(_)
                                _.innerText = 'Дата:'
                            })
                            l(_, 'input', _ => {
                                css`
                                    font-size: 16px;
                                    width: 115px;
                                    border: none;
                                     outline: none;
                                `.apply(_)
                                _.value = task.createDate
                                _.type = 'date'
                                _.onchange = ev => apiCall('/manager/task/editCreateDate',
                                    { 'id': task.id, 'date': (ev.target as HTMLInputElement).value })
                            })
                        })
                        l(_, 'div', _ => {
                            l(_, 'span', _ => {
                                css`
                                    &::before {
                                        height: 22px !important;
                                        top: 4px !important;
                                        background: url("/public/icons/task-manager.svg") no-repeat 0px 0px transparent;
                                    }
                                `.apply(_)
                                _.innerText = 'Ведущий:'
                            })
                            l(_, new Select<Leader | null>(
                                task.leaderId == null ? null : { id: task.leaderId, tgName: task.leaderTgName },
                                allLeaders,
                                leader => l('span', _ => {
                                    _.innerText = (leader == null ? 'Без ведущего' : leader.tgName)
                                }),
                                leader => apiCall('/manager/task/editLeader',
                                    { 'id': task.id, 'leaderId': leader == null ? null : leader.id }),
                                _ => {
                                    _.itemComparator = (l1, l2) => {
                                        if (l1 == null || l2 == null) {
                                            return l1 == l2
                                        }
                                        return l1.id == l2.id && l1.tgName == l2.tgName 
                                    }
                                }
                            ))
                        })
                        l(_, 'div', _ => {
                            l(_, 'span', _ => {
                                css`
                                    &::before {
                                        height: 22px !important;
                                        top: 4px !important;
                                        background: url("/public/icons/task-tags.svg") no-repeat 0px 0px transparent
                                    }
                                `.apply(_)
                                _.innerText = 'Тэги:'
                            })
                            lz(_, 'div', (_, z) => {
                                css`
                                    display: flex;
                                    gap: 8px;
                                    flex-wrap: wrap;
                                    align-items: baseline;
                                `.apply(_)

                                for (let tag of task.tags)
                                    l(_, new TagEdit(tag, async t => {
                                        await apiCall('/manager/task/deleteTag',
                                            { 'id': task.id, 'tagId': t.id })
                                        sync([z], task.tags = task.tags.filter(tt => tt != t))
                                    }))

                                let toAdd: Array<Tag> = []

                                l(_, new Modal(
                                    onFinish => l('div', _ => {
                                        css`
                                            width: min(80%, 960px);
                                            height: min(80%, 820px);
                                            background-color: white;
                                            display: flex;
                                            flex-direction: column;
                                            align-items: center;
                                            justify-content: space-between;
                                            border-radius: 30px;
                                            padding: 24px 32px;
                                            overflow-y: auto;
                                        `.apply(_)

                                        l(_, 'div', async _ => {
                                            css`
                                                gap: 10px;
                                                display: flex;
                                                flex-wrap: wrap;
                                            `.apply(_)
                                            const allTags = await apiCall<Array<Tag>>('/manager/tag/allowedTagsByTaskType', task.taskType)

                                            for (let tag of allTags)
                                                l(_, new TagAdd(tag, (t, isChecked) => {
                                                    if (isChecked) toAdd.push(t)
                                                    else toAdd = toAdd.filter(tt => tt != t)
                                                }))
                                        })

                                        l(_, 'div', _ => {
                                            css`
                                                align-self: flex-end;
                                                display: flex;
                                                gap: 10px;
                                            `.apply(_)

                                            l(_, 'button', _ => {
                                                stButtonOutline.apply(_)
                                                _.innerText = 'Отмена'
                                                _.onclick = __ => onFinish()
                                            })
                                            l(_, 'button', _ => {
                                                stButton.apply(_)
                                                _.innerText = 'Добавить'
                                                _.onclick = async __ => {
                                                    await apiCall('/manager/task/addTags',
                                                        { 'id': task.id, 'tagsId': toAdd.map(t => t.id) })
                                                    onFinish()
                                                    sync([z], [task.tags.push(...toAdd)])
                                                }
                                            })
                                        })
                                    }),
                                    l('span', _ => {
                                        css`
                                            background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" viewBox="0 0 10 10" fill="none"><g clip-path="url(%23clip0_661_859)"><path d="M5 0.357239V9.64295" stroke="black" stroke-linecap="round" stroke-linejoin="round"/><path d="M0.357178 4.9715H9.64289" stroke="black" stroke-linecap="round" stroke-linejoin="round"/></g><defs><clipPath id="clip0_661_859"><rect width="10" height="10" fill="white"/></clipPath></defs></svg>') no-repeat 4px 3px transparent;
                                            padding-left: 20px;
                                            line-height: 16px;
                                            font-size: 12px;
                                        `.apply(_)
                                        _.innerText = 'Добавить тэг'
                                    })
                                ))
                            })
                        })
                        l(_, 'div', _ => {
                            l(_, 'span', _ => {
                                css`
                                    &::before {
                                        height: 22px !important;
                                        top: 4px !important;
                                        background: url("/public/icons/task-telegram.svg") no-repeat 0px 0px transparent;
                                    }
                                `.apply(_)
                                _.innerText = 'Чат:'
                            })
                            l(_, 'span', _ => {
                                css`
                                    outline: none;
                                    &::after {
                                        content: '';
                                        display: inline-block;
                                        margin-left: 8px;
                                        height: 16px;
                                        width: 16px;
                                        background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="none"><g clip-path="url(%23clip0_99_1197)"><path d="M5.71439 13.9887L0.571533 15.4287L2.01153 10.2858L11.4287 0.914414C11.5351 0.805561 11.6623 0.719071 11.8025 0.660023C11.9428 0.600974 12.0936 0.570557 12.2458 0.570557C12.398 0.570557 12.5488 0.600974 12.6891 0.660023C12.8294 0.719071 12.9566 0.805561 13.063 0.914414L15.0858 2.9487C15.1929 3.05494 15.2779 3.18135 15.336 3.32061C15.394 3.45988 15.4239 3.60926 15.4239 3.76013C15.4239 3.911 15.394 4.06038 15.336 4.19965C15.2779 4.33892 15.1929 4.46532 15.0858 4.57156L5.71439 13.9887Z" stroke="black" stroke-opacity="0.25" stroke-linecap="round" stroke-linejoin="round"/></g><defs><clipPath id="clip0_99_1197"><rect width="16" height="16" fill="white"/></clipPath></defs></svg>');
                                    }
                                `.apply(_)

                                _.contentEditable = 'true'
                                _.innerText = task.chatLink
                                const chatLinkNode = _

                                _.addEventListener('input', debounce(() => {
                                    apiCall('/manager/task/editChat',
                                        { 'id': task.id, 'chat': chatLinkNode.innerText })
                                }))
                            })
                        })
                        l(_, 'div', _ => {
                            l(_, 'span', _ => {
                                css`
                                    &::before {
                                        height: 24px !important;
                                        top: 5px !important;
                                        background: url("/public/icons/task-volonteers.svg") no-repeat 0px 0px transparent;
                                    }
                                `.apply(_)
                                _.innerText = 'Волонтеры:'
                            })
                            l(_, 'span', _ => {
                                l(_, new Dropdown(task.volunteers.map(v =>
                                    l('a', _ => {
                                        _.href = this.volonteerEditLink.href(v.id)
                                        _.innerText = v.tgName
                                    })
                                )))
                            })
                        })
                        l(_, 'div', _ => {
                            l(_, 'span', _ => {
                                css`
                                    &::before {
                                        background: url("/public/icons/task-notes.svg") no-repeat 0px 0px transparent;
                                    }
                                `.apply(_)
                                _.innerText = 'Заметки:'
                            })
                            l(_, new TextArea(task.note, 'Введите текст заметки', _ => {
                                _.onkeyup = debounce((ev: Event) => {
                                    apiCall('/manager/task/editNote',
                                        { 'id': task.id, 'note': (ev.target as HTMLTextAreaElement).value })
                                })
                            }))
                        })
                    })
                    l(_, 'div', _ => {
                        css`border-radius: 16px`.apply(_)

                        const map = L.map(_).setView({ lng: task.longitude, lat: task.latitude }, 14)

                        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19,
                            attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
                        }).addTo(map);


                        L.control.scale({ imperial: false, metric: true }).addTo(map)


                        L.marker({ lng: task.longitude, lat: task.latitude })
                            //.bindPopup('The center of the world')
                            .addTo(map)

                        // WTF?
                        setTimeout(() => { map.invalidateSize() }, 100)

                    })
                })

                lz(_, 'div', (_, zGallery) => {
                    let showUpload = false
                    lz(_, 'div', (_, z) => {
                        css`
                            display: flex;
                            align-items: center;
                            gap: 8px;
                            margin-bottom: 16px;

                        `.apply(_)
                        l(_, 'span', _ => {
                            css`
                                cursor: pointer;
                                font-size: 24px;
                                font-weight: 600;
                            `.apply(_)
                            _.innerText = 'Фотоальбом'
                        })
                        l(_, 'span', _ => {
                            css`
                                cursor: pointer;
                                content: '';
                                width: 24px;
                                height: 24px;
                                display: inline-block;
                                background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="%2300000040" version="1.1" id="Capa_1" width="24px" height="24px" viewBox="0 0 304.899 304.899" xml:space="preserve" stroke="%2300000040"><g id="SVGRepo_bgCarrier" stroke-width="0"/><g id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"/><g id="SVGRepo_iconCarrier"><g><g><path d="M0.006,152.45c0,20.344,16.546,36.893,36.894,36.893h78.669v78.663c0,20.345,16.546,36.894,36.893,36.894 c20.351,0,36.894-16.549,36.894-36.894v-78.663H268c20.345,0,36.894-16.549,36.894-36.893c0-20.348-16.549-36.894-36.894-36.894 h-78.645V36.894C189.355,16.546,172.812,0,152.462,0c-20.347,0-36.893,16.546-36.893,36.894v78.663H36.9 C16.553,115.557,0.006,132.103,0.006,152.45z M121.718,127.854c3.401,0,6.149-2.756,6.149-6.149V36.894 c0-13.564,11.031-24.596,24.595-24.596c13.565,0,24.596,11.031,24.596,24.596v84.812c0,3.393,2.75,6.149,6.149,6.149H268 c13.565,0,24.596,11.03,24.596,24.596c0,13.564-11.03,24.596-24.596,24.596h-84.793c-3.399,0-6.149,2.756-6.149,6.148v84.812 c0,13.565-11.03,24.596-24.596,24.596c-13.564,0-24.595-11.03-24.595-24.596v-84.812c0-3.393-2.748-6.148-6.149-6.148H36.9 c-13.565,0-24.596-11.031-24.596-24.596c0-13.565,11.031-24.596,24.596-24.596H121.718z"/></g></g></g></svg>') no-repeat;                        
                            `.apply(_)
                            _.onclick = () => sync([z], [showUpload = !showUpload])
                        })

                        if (showUpload) {
                            l(_, 'input', _ => {
                                _.type = 'file'
                                _.onchange = async ev => {
                                    const files = (ev.target as HTMLInputElement).files!
                                    if (files.length != 0) {
                                        const form = new FormData()
                                        form.append("file", files[0])
                                        form.append("taskId", task.id + '')
                                        const photo = await apiCall<Photo>('/manager/task/photoUpload', form)

                                        sync([zGallery], [task.photos.push(photo)])
                                    }
                                }
                            })
                        }
                    })


                    l(_, 'div', _ => {
                        css`
                            display: flex;
                            flex-wrap: wrap;
                            gap: 24px;

                            &>img {
                                min-width: 300px;
                                max-width: calc(100% / 3);
                                height: auto;
                                flex-grow: 1;
                            }
                        `.apply(_)

                        for (let photo of task.photos)
                            l(_, 'div', _ => {
                                css`position: relative`.apply(_)

                                l(_, new ConfirmModal(
                                    'Удалить изображение?',
                                    l('div', _ => {
                                        css`
                                        background-color: white;
                                        border-radius: 4px;
                                        position: absolute;
                                        width: 20px;
                                        height: 20px;
                                        top: 4px;
                                        right: 4px;
                                    `.apply(_)

                                        l(_, 'span', _ => {
                                            css`
                                            content: '';
                                            display: inline-block;
                                            width: 20px;
                                            height: 20px;
                                            background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-x"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>');
                                        `.apply(_)
                                        })
                                    }),
                                    async () => {
                                        await apiCall('/manager/task/photoDelete', photo.photoId)
                                        sync([zGallery], [task.photos = task.photos
                                            .filter(p => p.photoId != photo.photoId)])
                                    }
                                ))

                                l(_, new Modal(
                                    onFinish => l('div', _ => {
                                        css`
                                            max-width: 90vw;
                                            max-height: 90vh;
                                            display: flex;
                                            justify-content: center;
                                            position: relative;
                                            &>img {
                                                max-width:100%;
                                                max-height:100%;
                                                object-fit: contain;
                                            }
                                        `.apply(_)

                                        l(_, 'div', _ => {
                                            css`
                                                background-color: white;
                                                border-radius: 4px;
                                                position: absolute;
                                                width: 32px;
                                                height: 32px;
                                                top: 8px;
                                                right: 8px;
                                            `.apply(_)

                                            l(_, 'span', _ => {
                                                css`
                                                    content: '';
                                                    display: inline-block;
                                                    width: 32px;
                                                    height: 32px;
                                                    background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-minimize-2"><polyline points="4 14 10 14 10 20"/><polyline points="20 10 14 10 14 4"/><line x1="14" y1="10" x2="21" y2="3"/><line x1="3" y1="21" x2="10" y2="14"/></svg>');
                                                `.apply(_)
                                            })

                                            _.onclick = onFinish
                                        })

                                        l(_, 'img', _ => { _.src = '/images/' + photo.photoPath })
                                    }),
                                    l('img', _ => { _.src = '/images/' + photo.optimizedPhotoPath })
                                ))
                            })
                    })
                })
            })
        })
    }
}