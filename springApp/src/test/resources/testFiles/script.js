/*
1. drawer рисует кривую Безье, обрабатывает иные действия с рисовалкой
2. При необходимости и возможности инициализирует button 
3. button в свою очерень инициализирует gCoder,
4. gCoder берет необходимые данные из drawer (сепаратор и координаты кружков) и формирует G-коды
*/

class Drawer {
    
    #separator = 500; // Количество точек в кривой Безье - можно менять
    #canvas; // Объект канвас
    #ctx // Его контекст
    #absolute_click_counter = 0; // Сколько кружков создано на рисовалке
    #circles = []; // Массив с координатами кружков
    #pink_circle = []; // Массив с координатами и номером кружка, над которым мышь
    #drag_flag = false; // Зажата ли мышь над каким-лиюо кружком

    constructor() { // Добавляем при создании объекта класса все необходимые ЭвентЛистенеры
        this.#canvas = document.getElementById('canvas');
        this.#ctx = canvas.getContext('2d');
        this.#canvas.addEventListener('mouseover', this.#mouseover.bind(this)); // Мышь появляется над рисовалкой
        this.#canvas.addEventListener('mouseout', this.#mouseout.bind(this)); // Мышь уходит с рисовалки
        this.#canvas.addEventListener('click', this.#mouse_click.bind(this)); // Отслеживание клика
        this.#canvas.addEventListener('mousemove', this.#mousemove.bind(this)); // Мышь двигается над рисовалкой. Для поиска сиреневых кружков и отрисовки их передвижения
        this.#canvas.addEventListener('mousedown', this.#mousedown.bind(this)); // Если зажали мышь для перемещения кружка над рисовалкой
        this.#canvas.addEventListener('mouseup', this.#mouseup.bind(this)); // Если отпустили мышь над рисовалкой
        this.#canvas.addEventListener('contextmenu', this.#contextmenu.bind(this)); // Если нажали правую кнопку над рисовалкой
    }

    #mouseover() { // Мышь появляется над рисовалкой
        this.#canvas.style = 'border-style: dashed';
    }

    #mouseout() { // Мышь уходит с рисовалки
        this.#canvas.style = 'border-style: solid';

        if (this.#drag_flag) { // Если происходит драг, то отменяем его
            this.#drag_flag = false;
            this.#pink_circle = [];
            this.#bezier();
        }
    }

    #mousemove(e) { // Двигаем мышь над рисовалкой. Обработка поиска сиреневых кружков и перетаскивания кружков
        let x = e.offsetX; // Координаты мыши
        let y = e.offsetY;

        if (this.#drag_flag != false && this.#pink_circle.length != 0) {
            this.#circles[this.#pink_circle[2]][0] = x;
            this.#circles[this.#pink_circle[2]][1] = y;
            this.#bezier();
            this.#draw_pink_circle(x, y);
            return;
        }

        let find_flag = false; // Флаг есть ли под курсором какой-либо кружок
        for (let i in this.#circles) { // Смотрим, есть ли под курсором кружек
            if (Math.pow(x-this.#circles[i][0], 2) + Math.pow(y - this.#circles[i][1], 2) <= Math.pow(7, 2)) {
                this.#pink_circle = [];
                this.#pink_circle.push(this.#circles[i][0]);
                this.#pink_circle.push(this.#circles[i][1]);
                this.#pink_circle.push(i);
                this.#draw_pink_circle(this.#circles[i][0], this.#circles[i][1]); // Если есть, то отрисовываем
                find_flag = true;
                break;
            }
        }
        if (find_flag == false) { 
            if (this.#pink_circle.length != 0) {
                this.#draw_pink_circle(this.#pink_circle[0], this.#pink_circle[1], false); // Если нет, то отрисовываем сиреневые кружки обратно в черные, если таковые имеются
                this.#pink_circle = [];
            }
        }
    }

    #mousedown() { // Зажали мышь
        if (this.#pink_circle.length != null) {
            this.#drag_flag = true; // Если над кружком, то инициализируем перетаскивание
        }
    }

    #mouseup() { // Отпустили мышь
        this.#drag_flag = false; // Запрещаем перетаскивание
    }

    #contextmenu(e) { // Если жмем правую кнопку мышь
        e.preventDefault(); // Убираем базовое поведение
        if (this.#pink_circle.length != 0) { // Если есть какой-нибудь фиолетовый кружек
            this.#circles.splice(this.#pink_circle[2], 1) // Удаляем его из основного массива
            this.#absolute_click_counter -= 1; // Количество кружков уменьшаем
            this.#pink_circle = []; // Сиреневых кружков теперь нет
            if (this.#absolute_click_counter > 2) { // Если кружков все еще больше 2, то перерисовываем кривую Безье
                this.#bezier();
            } 
            else {
            this.#ctx.clearRect(0, 0, this.#canvas.width, this.#canvas.height);
            for (let i in this.#circles) {
                this.#draw_circle(this.#circles[i][0], this.#circles[i][1], Number(i) + 1); // Если меньше двух, то отрисовываем только кружки
            }
            button.button_off(); // И отключаем кнопку "Получить G-коды"
            }
        }
    }

    #mouse_click(e) { // Клик мыши по рисовалке
        if (this.#pink_circle.length != 0 || this.#drag_flag) { // Если происходит драг или мышь находится над другим кружком, рисовать ничего не надо
            return;
          }

        this.#absolute_click_counter += 1;
        let x_coord = e.offsetX
        let y_coord = e.offsetY
        this.#circles.push([x_coord, y_coord]); // Записываем координаты клика в массив
        
        this.#draw_circle(x_coord, y_coord, this.#absolute_click_counter); // рисуем кружек
        if (this.#absolute_click_counter > 2) { // Перерисовываем кривую Безье, если кружков больше двух 
            this.#bezier();
          }
    }

    #draw_circle(x, y, number) { // Отрисовка обычного кружка с номером
        this.#ctx.beginPath();
        this.#ctx.arc(x, y, 5, 0, 2 * Math.PI);
        this.#ctx.font = "10px serif";
        this.#ctx.fillStyle = "black";
        this.#ctx.strokeStyle="black";
        this.#ctx.fillText(number, x - 4, y - 12);
        this.#ctx.stroke();
    }

    #bezier(except = null) { // Отрисовка кривой Безье, except для случая перетаскивания кружков
        this.#ctx.clearRect(0, 0, this.#canvas.width, this.#canvas.height); // удаляем все
        
        for (let i in this.#circles) { // Рисуем все кружки, кроме того, что перетаскиваем, если перетаскиваем
            if (except == i) {
                continue;
            }
            this.#draw_circle(this.#circles[i][0], this.#circles[i][1], Number(i) + 1);
        }
        
        if (this.#absolute_click_counter > 2) { // Костыль, чтоб не рисовало кривую Безье во время перетаскивания первых двух кружков
            for (let i = 0; i < this.#separator; i++) { // #separator раз отрисовываем различные точки кривой Безье
                this.#draw_bezier(i);
            }
            button.button_on(); // Включаем кнопку "Получить G-коды"
        }
    }

    #draw_bezier(i) { // Отрисовка i-ой точки кривой Безье
        let points = this.#circles.concat();
        while (points.length != 1) { // Делим линии пропорционально i до тех пор, пока не останется одна точка
            let temp_var = points.concat();
            points = [];     
            for (let j = 1; j < temp_var.length; j++) {
              let temp_x = ((temp_var[j][0] - temp_var[j - 1][0]) / this.#separator) * i + temp_var[j - 1][0];
              let temp_y = ((temp_var[j][1] - temp_var[j - 1][1]) / this.#separator) * i + temp_var[j - 1][1];
              points.push([temp_x, temp_y])
            }
          }
        this.#ctx.beginPath();
        this.#ctx.fillRect(points[0][0], points[0][1], 2, 2) // Отрисовываем эту точку
    }

    #draw_pink_circle(x, y, pink = true) { // Отрисовка кружка без номера. По умолчанию - сиреневого, false - черного
        let ctx = this.#ctx;
        ctx.beginPath();
        ctx.clearRect(x - 6, y - 6, 12, 12);
        ctx.arc(x, y, 5, 0, 2 * Math.PI);
        ctx.strokeStyle="#834e9c";
        if (pink == false) {
            ctx.strokeStyle="black";
        }
        ctx.stroke();
    }

    get_separator() { // Необходимо для связи между Drawer и GCoder
        return this.#separator;
    }

    get_cirscles() { // Необходимо для связи между Drawer и GCoder
        return this.#circles;
    }

    get_canvas_height() {
        return this.#canvas.height;
    }

    get_canvas_width() {
        return this.#canvas.width;
    }

    draw_help_rect(x, y, color = "#834e9c") {
        y = this.#canvas.height - y;
        this.#ctx.beginPath();
        this.#ctx.fillStyle = color;
        if (color != "#834e9c") {
            this.#ctx.fillRect(x, y, 6, 6);
        } else {
            this.#ctx.fillRect(x, y, 2, 2);
        }
        this.#ctx.stroke();
    }
    clear() {
        this.#ctx.clearRect(0, 0, this.#canvas.width, this.#canvas.height);
    }
} 


class GCodesButton { // Кнопочка
    #button_div; // Контейнер кнопочки
    #elem; // Сама кнопочка
    #gCodesFunction = gCoder.get_g_codes.bind(gCoder); // Функция, вызывающая формирование G-кодов у объекта gCoder
    
    constructor() {
        this.#button_div = document.getElementById('button_div'); // Ищем контейнер кнопочки
    }

    button_on() { // Функция, включающая кнопку
        this.#button_div.innerHTML = '<div id="button">Получить G-коды</div>'; // Добавление HTML
        this.#elem = document.getElementById('button'); // Поиск добавленной кнопочки
        this.#elem.addEventListener('click', this.#gCodesFunction) // Навешивание на нее eventListenera
        gCoder.g_codes_list_off(); // Удаление предыдущих G-кодов
    }

    button_off() { // Функция, выключающая кнопку
        this.#elem.removeEventListener('click', this.#gCodesFunction); // Удаление eventListenera
        this.#button_div.innerHTML = ''; // Удаление HTML
        gCoder.g_codes_list_off(); // Удаление предыдущих G-кодов
    }
}


class GCoder { // Тут все, что связано с G-кодами и их обработкой
    #g_codes_div; // Контейнер для отрисовки списка G-кодов
    #separator; // Сепаратор, берется из Drawer
    #circles; // Кружки, так же беруться из Drawer
    #g_code_html; // Строка под G-коды
    #g_codes = []; // Сами G-коды в формате [X, Y] (3 знака после запятой)
    #final_g_codes = []; // G-коды после поиска линий и окружностей

    constructor() {
        this.#g_codes_div = document.getElementById('g_codes'); // При создании объекта ищем контейнер для G-кодов
    }

    g_codes_list_off() { // Функция для удаления предыдущих G-кодов
        this.#g_codes_div.innerHTML = '';
    }

    get_g_codes() { // Функция получения G-кодов, срабатывает после нажатия на кнопку "Получить G-коды"

        button.button_off(); // Отключаем кнопку
        
        this.#separator = drawer.get_separator(); // Получаем из drawer сепаратор и координаты кружков
        this.#circles = drawer.get_cirscles();

        this.#g_codes = []; // Удаляем предыдущие G-коды
        this.#final_g_codes = [];
        for (let j = 0; j < this.#separator; j++) { // Формируем G-коды аналогично линии Безье
            let points = this.#circles.concat();
            while (points.length != 1) {
        
                let temp_var = points.concat();
                points.length = 0;
            
                for (let i = 1; i < temp_var.length; i++) {
                    let temp_x = ((temp_var[i][0] - temp_var[i - 1][0]) / this.#separator) * j + temp_var[i - 1][0];
                    let temp_y = ((temp_var[i][1] - temp_var[i - 1][1]) / this.#separator) * j + temp_var[i - 1][1];
                    points.push([temp_x, temp_y])
                }
            }
            this.#g_codes.push([points[0][0], (canvas.height - points[0][1])]);
          }
        // this.#separate_g_codes(this.#g_codes); // Можно разбить линию Безье на равные промежутки по расстоянию
        this.#g_codes_for_circles(); // А можно и не разбивая найти сразу все линии и дуги окружностей
        this.#print_g_codes(); // Отрисовываем
    }
    

    #g_codes_for_circles() {
        let separate_codes = this.#g_codes;
        let k = (separate_codes[0][1] - separate_codes[1][1]) / (separate_codes[0][0] - separate_codes[1][0]); // Коэф. касательной, относит. которой смотрим
        let b = separate_codes[1][1] - k * separate_codes[1][0];
        let k_i = 0; // Номер точки начала окружности. Выше коэф. касательной к этой точке
        let k2; // Коэф. касательной к i точке предполагаемой окружности
        let b2;
        let k3; // Коэф. прямой, перпендикулярной к точке второй касательной
        let b3;
        let k4; // Коэф. прямой, перпендикулярной к точке первой касательной
        let b4;
        let d; // Расстояние по перпендикуляру до первой касательной (радиус 1)
        let d2; // Расстояние по перпендикуляру до второй касательной (радиус 2)
        let x; // Точки пересечения радиусов (координаты центра окружности)
        let y;
        let direction;
        for (let i = 3; i < separate_codes.length; i++) { // Перебираем всю кривую, если находится окружность/прямая, перезаписываем первую касательную
            k2 = (separate_codes[i-1][1] - separate_codes[i][1]) / (separate_codes[i-1][0] - separate_codes[i][0]); // Коэф. второй касательной
            b2 = separate_codes[i][1] - k2 * separate_codes[i][0];
            k3 = -1 / k2; // Перпендикуляр к k2, проходящий через дугу окружности
            b3 = (separate_codes[i][0] / k2) + separate_codes[i][1]; 
            k4 = -1 / k; // Перпендикуляр к k, проходящий через дугу окружности
            b4 = (separate_codes[k_i][0] / k) + separate_codes[k_i][1]; 
            x = (b4 - b3) / (k3 - k4); // Точки пересечения двух перпендикуляров
            y = k3 * x + b3;
            d = Math.sqrt(Math.pow((x - separate_codes[k_i][0]), 2) + Math.pow((y - separate_codes[k_i][1]), 2)); // Расстояние до k
            d2 = Math.sqrt(Math.pow((x - separate_codes[i][0]), 2) + Math.pow((y - separate_codes[i][1]), 2)); // Расстояние до k2
            if ((Math.abs(d-d2) > 0.5) || (i == (separate_codes.length - 1))) { // Если разница в двух радиусах > 0.5
                if (d > 700) { // Если радиус > 700, то принимаем за прямую, меньше - за окружность
                    console.log('ПРЯМАЯ КОНЧИЛАСЬ');
                    drawer.draw_help_rect(separate_codes[i][0], separate_codes[i][1], "blue");
                    this.#final_g_codes.push(["line", separate_codes[i][0], separate_codes[i][1]]);
                } else { // Если окружность, то определяем ее направление
                    console.log('Окружность кончилась');
                    drawer.draw_help_rect(separate_codes[i][0], separate_codes[i][1], "red");
                    direction = this.#direction(x, y, separate_codes[k_i][0], separate_codes[k_i][1], separate_codes[k_i + 1][0], separate_codes[k_i + 1][1], separate_codes[i][0], separate_codes[i][1]);
                    this.#final_g_codes.push(["circle", separate_codes[i][0], separate_codes[i][1], (d + d2) / 2, direction]);
                }
                k = (separate_codes[i-1][1] - separate_codes[i][1]) / (separate_codes[i-1][0] - separate_codes[i][0]);
                b = separate_codes[i][1] - k * separate_codes[i][0]; // Перезаписываем первую касательную на данную
                k_i = i;
                if (i + 2 <= separate_codes.length) { // Лишний раз убирает неопределенность в работе программы. Можно и без этого
                    i += 2;
                }
            }
        }
        
    }

    #direction(x_r, y_r, x1, y1, x2, y2, x3, y3) { // Определение направления окружности
        x1 = x1 - x_r; // Приводим все координаты, как будто бы центр окружности - это 0,0
        y1 = y1 - y_r;
        x2 = x2 - x_r;
        y2 = y2 - y_r;
        x3 = x3 - x_r;
        y3 = y3 - y_r;
        let angle_1 = this.#angle(x1, y1); // Определяем четверти и углы. Угол определяем по числовой окружности как в школе
        let angle_2 = this.#angle(x2, y2);
        let angle_3 = this.#angle(x3, y3);
        let squere_1 = this.#squere(x1, y1);
        let squere_2 = this.#squere(x2, y2);
        let squere_3 = this.#squere(x3, y3);
        if (squere_1 == squere_2 && squere_2 == squere_3) { // Если четверти одинаковы, то определяем по разнице между первым и последним углом
            if (angle_3 > angle_1) {
                console.log("Против")
                return "G03";
            } else {
                console.log("По")
                return "G02";
            }
        } else { // Если не одинаковы, то определяем по разнице очень близких друг к другу точек, уповая на то, что перехода 1-4 четверти не было
            if (angle_2 > angle_1) {
                console.log("Против")
                return "G03";
            } else {
                console.log("По")
                return "G02";
            }
        }
        
    }

    #squere(x1, y1) { // Функция определения четверти
        if (x1 > 0) {
            if (y1 > 0) {
                return 1;
            } else {
                return 4;
            }
        } else {
            if (y1 > 0) {
                return 2;
            } else {
                return 3;
            }
        }
    }

    #angle(x, y) { // Функция определения угла
        let squere = this.#squere(x, y);
        let tangens;
        let res_angle;
        if (squere == 1) {
            tangens = y / x;
            res_angle = Math.atan(tangens) * (180 / Math.PI);
            return res_angle
        }
        if (squere == 2) {
            tangens = Math.abs(x) / Math.abs(y);
            res_angle = Math.atan(tangens) * (180 / Math.PI) + 90;
            return res_angle
        }
        if (squere == 3) {
            tangens = Math.abs(y) / Math.abs(x);
            res_angle = Math.atan(tangens) * (180 / Math.PI) + 180;
            return res_angle
        }
        if (squere == 4) {
            tangens = Math.abs(x) / Math.abs(y);
            res_angle = Math.atan(tangens) * (180 / Math.PI) + 270;
            return res_angle
        }
    }

    #separate_g_codes(g_codes){ // Функция разбиения линии Безье на равные по расстоянию промежутки, не используется
        let r = 0;              // Можно использовать, если будут слишком маленькие отрезки
        let temp_r = 0;
        let counter = 0;
        drawer.clear();
        let separate_codes = [];
        for (let i = 1; i < g_codes.length; i++) {
            temp_r = Math.sqrt(Math.pow((g_codes[i][0] - g_codes[i-1][0]), 2) + Math.pow((g_codes[i][1] - g_codes[i-1][1]), 2));
            r += temp_r
            counter += 1;
            if (r > 5 && counter > 4) {
                r = 0;
                counter = 0;
                drawer.draw_help_rect(g_codes[i][0], g_codes[i][1]);
                separate_codes.push([g_codes[i][0], g_codes[i][1]]);
            }
        }
        // this.#g_codes_for_lines(separate_codes);
    }

    #g_codes_for_lines(separate_codes) { // Разбиение только на линии, без окружностей
        let k = (separate_codes[0][1] - separate_codes[1][1]) / (separate_codes[0][0] - separate_codes[1][0]); // Коэф. прямой, относит. которой смотрим
        let b = separate_codes[1][1] - k * separate_codes[1][0];
        let d;
        let k2;
        let b2;
        let x;
        let y;
        console.log(k, b);
        for (let i = 1; i < separate_codes.length; i++) {
            k2 = -1 / k; // Коэф. прямой, перпендикулярной к данной и проходящей через точку i
            b2 = (separate_codes[i][0] / k) + separate_codes[i][1]; 
            x = (b2 - b) / (k - k2); // Точки пересечения двух прямых
            y = k * x + b;
            d = Math.sqrt(Math.pow((x - separate_codes[i][0]), 2) + Math.pow((y - separate_codes[i][1]), 2)); // Расстояние между точками
            console.log(b, d);
            if (d > 0.5) {
                console.log("Прямая кончилась");
                k = (separate_codes[i-1][1] - separate_codes[i][1]) / (separate_codes[i-1][0] - separate_codes[i][0]); // Коэф. прямой, относит. которой смотрим
                b = separate_codes[i][1] - k * separate_codes[i][0];
                drawer.draw_help_rect(separate_codes[i][0], separate_codes[i][1], true);
            }
        }
    }

    #print_g_codes() { // Отрисовка G-кодов
        let codes = this.#final_g_codes;
        let g_codes = this.#g_codes;
        this.#g_code_html = `<p>G17</p><p>G91</p><p>G00 X${g_codes[0][0]} Y${g_codes[0][1]}</p>`;
        for (let i in codes){
            if (codes[i][0] == "line") {
                this.#g_code_html = this.#g_code_html + `<p>G01 X${codes[i][1].toFixed(3)} Y${codes[i][2].toFixed(3)} F400</p>`
            } else {
                this.#g_code_html = this.#g_code_html + `<p>${codes[i][4]} X${codes[i][1].toFixed(3)} Y${codes[i][2].toFixed(3)} R${codes[i][3].toFixed(3)} F400</p>`
            }
        }
        this.#g_codes_div.innerHTML = `<div id="show_g_codes">${this.#g_code_html}</div>`;
    }
}

let drawer = new Drawer(); // Создаем объект класса Drawer (сама рисовалка)
let gCoder = new GCoder(); // Создаем объект класса GCoder (формировщик G-кодов)
let button = new GCodesButton(); // Создаем объект класса GCodesButton (кнопочка)

/*
1. drawer рисует кривую Безье, обрабатывает иные действия с рисовалкой
2. При необходимости и возможности инициализирует button 
3. button в свою очерень инициализирует gCoder,
4. gCoder берет необходимые данные из drawer (сепаратор и координаты кружков) и формирует G-коды
*/