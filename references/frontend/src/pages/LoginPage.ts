import { apiCall, css, l, lz, sync, type ClassComponent } from "../lib";
import { Button } from "../uikit/Button";
import { stInput } from "../uikit/Input";

export class LoginPage implements ClassComponent<HTMLDivElement> {
    mount(): HTMLDivElement {
        return l('div', _ => {
            css`
                position: fixed;
                width: 100%;
                height: 100%;
                top: 0;
                left: 0;
                background-color: #F872441A;
                display: flex;
                justify-content: center;
                align-items: center;
            `.apply(_)

            l(_, 'img', _ => {
                css`
                    position: fixed;
                    top: 0;
                    left: 24px;
                
                    width: 225px;
                    height: 104px;
                    content: url('/public/icons/logo.svg');
                `.apply(_)
            })

            let login = ''
            let code = ''
            let error: string | null = null
            let action: 'Code' | 'Login' = 'Code'

            lz(_, 'div', (_, z) => {
                css`
                    width: 365px;
                    min-height: 390px;
                    background-color: white;
                    border-radius: 30px;
                    padding: 32px;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    gap: 40px;
                `.apply(_)

                l(_, 'span', _ => {
                    _.innerText = 'Вход'
                    css`
                        font-size: 32px;
                        font-weight: bold;
                    `.apply(_)
                })

                l(_, 'span', _ => {
                    css`color: #bc0a2c`.apply(_)

                    if (error != null)
                        _.innerText = error
                    else
                        _.style.display = 'none'
                })


                l(_, 'div', _ => {
                    stInput.apply(_)

                    l(_, 'span', _ => {
                        css`         
                            background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 20 20" fill="none"><g clip-path="url(%23clip0_794_972)"><path d="M19.2857 15V17.8571C19.2857 18.236 19.1351 18.5994 18.8673 18.8673C18.5994 19.1351 18.236 19.2857 17.8571 19.2857H15" stroke="black" stroke-linecap="round" stroke-linejoin="round"/><path d="M15 0.714279H17.8571C18.236 0.714279 18.5994 0.864789 18.8673 1.1327C19.1351 1.40061 19.2857 1.76396 19.2857 2.14285V4.99999" stroke="black" stroke-linecap="round" stroke-linejoin="round"/><path d="M0.714355 4.99999V2.14285C0.714355 1.76396 0.864865 1.40061 1.13277 1.1327C1.40068 0.864789 1.76404 0.714279 2.14293 0.714279H5.00007" stroke="black" stroke-linecap="round" stroke-linejoin="round"/><path d="M5.00007 19.2857H2.14293C1.76404 19.2857 1.40068 19.1351 1.13277 18.8673C0.864865 18.5994 0.714355 18.236 0.714355 17.8571V15" stroke="black" stroke-linecap="round" stroke-linejoin="round"/><path d="M9.99971 9.28568C11.5776 9.28568 12.8568 8.0065 12.8568 6.42856C12.8568 4.85061 11.5776 3.57143 9.99971 3.57143C8.42175 3.57143 7.14258 4.85061 7.14258 6.42856C7.14258 8.0065 8.42175 9.28568 9.99971 9.28568Z" stroke="black" stroke-linecap="round" stroke-linejoin="round"/><path d="M15.4333 15.7144C15.0641 14.5634 14.3391 13.5595 13.3627 12.8473C12.3863 12.135 11.2089 11.7512 10.0003 11.7512C8.7917 11.7512 7.61431 12.135 6.63788 12.8473C5.66145 13.5595 4.93645 14.5634 4.56738 15.7144H15.4333Z" stroke="black" stroke-linecap="round" stroke-linejoin="round"/></g><defs><clipPath id="clip0_794_972"><rect width="20" height="20" fill="white"/></clipPath></defs></svg>');
                        `.apply(_)
                    })
                    l(_, 'input', _ => {
                        _.value = login
                        _.placeholder = 'Логин'
                        _.onchange = __ => login = _.value
                        if (action == 'Login')
                            _.disabled = true
                    })
                })

                l(_, 'div', _ => {
                    css`height: 40px`.apply(_)
                    if (action == 'Code') return

                    stInput.apply(_)


                    l(_, 'span', _ => {
                        css`                    
                            background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="16" height="20" viewBox="0 0 16 20" fill="none"><path d="M13.7141 7.85713H2.28551C1.49653 7.85713 0.856934 8.49673 0.856934 9.2857V17.8571C0.856934 18.6461 1.49653 19.2857 2.28551 19.2857H13.7141C14.5031 19.2857 15.1426 18.6461 15.1426 17.8571V9.2857C15.1426 8.49673 14.5031 7.85713 13.7141 7.85713Z" stroke="black" stroke-linecap="round" stroke-linejoin="round"/><path d="M13 7.85714V5.71428C13 4.38819 12.4733 3.11642 11.5355 2.17875C10.5979 1.24106 9.32609 0.714279 8 0.714279C6.67391 0.714279 5.40214 1.24106 4.46447 2.17875C3.52679 3.11642 3 4.38819 3 5.71428V7.85714" stroke="black" stroke-linecap="round" stroke-linejoin="round"/><path d="M7.99993 14.2857C8.39442 14.2857 8.71422 13.9659 8.71422 13.5714C8.71422 13.1769 8.39442 12.8571 7.99993 12.8571C7.60544 12.8571 7.28564 13.1769 7.28564 13.5714C7.28564 13.9659 7.60544 14.2857 7.99993 14.2857Z" stroke="black" stroke-linecap="round" stroke-linejoin="round"/></svg>');
                            background-repeat: no-repeat;
                            margin-left: 1px;
                        `.apply(_)
                    })
                    l(_, 'input', _ => {
                        _.value = code
                        _.placeholder = 'Код'
                        _.onchange = __ => code = _.value
                    })
                })

                const callErrorHandling = async (f: () => Promise<void>) => {
                    try { await f() } catch (e) {
                        sync([z], [error = (e as Error).message])
                    }
                }

                l(_, 'div', _ => {
                    l(_, new Button(
                        action == 'Code' ? 'Получить код' : 'Войти',
                        'Normal', el => {
                            css`width: 157px`.apply(el)

                            el.onclick = async () => {
                                if (action == 'Code') {
                                    await callErrorHandling(async () => {
                                        if (login == '')
                                            throw new Error('Введите имя пользователя')

                                        await apiCall('/auth/requestCode', login)
                                        sync([z], [action = 'Login', error = null])
                                    })
                                } else {
                                    await callErrorHandling(async () => {
                                        if (code == '')
                                            throw new Error('Введите код')
                                        await apiCall('/auth/login', code)
                                        window.location.hash = '#/'
                                    })
                                }
                            }
                        }
                    ))
                })
            })
        })
    }
}