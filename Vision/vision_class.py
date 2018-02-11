# ------------launch options------------------------------------------------------
from clint.textui import colored
import sys
from flask import Flask, render_template, Response

camera = 0
if '-h' in sys.argv or '--help' in sys.argv:
    print(
        colored.cyan('Usage: ') + 'python3 vision_class.py [-s / --stream] [-l / --local] [-p / --port {camera port}]  '
                                  '[-nts / --networktables-server {networktable ip address}]')
    exit(0)  #shows the help page if -h argument is present

if '-s' in sys.argv or '--stream' in sys.argv: #checks for the stream argument
    is_stream = True
else:
    is_stream = False

if '-l' in sys.argv or '--local' in sys.argv: # checks for the local argument
    is_local = True
else:
    is_local = False

if '-p' in sys.argv or '--port' in sys.argv: #checks for the port argument and sets the port as the argument after it
    try:
        try:
            index = sys.argv.index('-p')
        except:
            index = sys.argv.index('--port')
        camera = int(sys.argv[index + 1])
    except:
        print(colored.red('ERROR: Not A Valid Camera Port'))
        exit(11)
else:
    camera = 0

if '--mask' in sys.argv or '-m' in sys.argv:
    mask=True
else:
    mask = False

if '-nts' in sys.argv or '--networktables-server' in sys.argv: #sets the netwrorktables server as the argument after this one
    try:
        index = sys.argv.index('-nts')
    except:
        index = sys.argv.index('--networktabless-server')
    try:
        nt_server = sys.argv[index + 1]
        listed_nt = list(nt_server)
        if listed_nt.count('.') == 0:
            print(colored.red('ERROR: You Must Enter A Valid IP Address'))
            exit(12)
    except IndexError:
        print(colored.red('ERROR: You Must Enter A Valid IP Address'))
        exit(12)
else:
    nt_server = "roboRIO-{team_number}-FRC.local".format(team_number=5987)
print('NetworkTables Server: ' + colored.green(nt_server))

# ------------getting the ip------------------------------------------------------
import netifaces as ni

ip = None
for interface in ni.interfaces():
    if ip is None:
        try:
            ip = ni.ifaddresses(interface)[ni.AF_INET][0]['addr']
            start = ip.split('.')[0]
            if start == '127':
                ip = None
        except KeyError:
            pass
    else:
        break
print('IP: ' + colored.green(ip))
# -----------------------------Starting The Vision Class--------------------------------------------------------------

import cv2
import numpy as np
import math
from networktables import NetworkTables
from time import sleep
cam = cv2.VideoCapture(camera)

stop = False

class Vision:
    global stop
    def __init__(self,surfix=''): # the surfix is for all the value files this class will use
        self.surfix=surfix
        self.cam = cam

        self.distance = 0
        self.angle = 0
        self.sees_target = False

        try: # makes sure the camera works, if not, throws an error
            self.get_frame(once=True)
        except AttributeError:
            print(colored.red('ERROR: Camera Not Connected or In Use'))
            exit(13)

        NetworkTables.initialize(server=nt_server)
        self.table = NetworkTables.getTable("Vision") # initialize Vision networktables
        sleep(1)
        self.check_files() # make sure the files with the surfix exist, if not, create them
        self.set_range() # set the hsv range
        self.init_values() # set all the values
        self.filter_hsv() # convert image from bgr to hsv
        self.dirode() # dialate and erode
        _, contours, _ = cv2.findContours(self.mask.copy(), cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE) # get the contours
        self.contours = list(contours)
        self.hulls = [] # start a list of hulls, if needed
        self.centers = [] # start a list of centers, if needed
        self.font = cv2.FONT_HERSHEY_SIMPLEX
        self.stream = [] # empty stream
        self.app = Flask(__name__) # the app used for streaming
        self.set_item("Filter Mode",self.surfix)
        @self.app.route('/')
        def index():#returns the HTML template (lower case 't')
            return render_template('index.html')

        @self.app.route('/video_feed')
        def video_feed(): #initiate the feed
            return Response(self.gen(),
                        mimetype='multipart/x-mixed-replace; boundary=frame')
        if mask:
            @self.app.route('/mask_feed')
            def mask_feed():
                return Response(self.gen_mask(), mimetype='multipart/x-mixed-replace; boundary=frame')
        else:
            @self.app.route('/mask_feed')
            def mask_feed():
                return "Mask Mode Not Enabled"
    def set_item(self, key, value):
        """
        Summary: Add a value to SmartDashboard.

        Parameters:
            * key : The name the value will be stored under and displayed.
            * value : The information the key will hold.
            * value_type : The type of the value it recieved (string, integer, boolean, etc.).
        """
        value_type = type(value)
        if value_type is str:
            self.table.putString(key, value)
        elif value_type is int or value_type is float:
            self.table.putNumber(key, value)
        elif value_type is bool:
            self.table.putBoolean(key, value)

    def get_item(self, key, default_value):
        """
        Summary: Get a value from SmartDashboard.

        Parameters:
            * key : The name the value is stored under.
            * default_value : The value returned if key holds none.
        """
        try:
            res = self.table.getString(key, default_value)
        except:
            try:
                res = self.table.getNumber(key, default_value)
            except:
                res = self.table.getBoolean(key, default_value)
        return res

    # def range_finder(self):
    #     minH = 255
    #     minS = 255
    #     minV = 255
    #     maxH = 0
    #     maxS = 0
    #     maxV = 0
    #     height, width, _ = self.frame.shape
    #     target = np.zeros((height, width, 1), dtype=np.uint8)
    #     target[int(height / 3):int(2 * height / 3), int(2 * width / 6):int(4 * width / 6)] = 255
    #     target1 = target # crop the frame
    #     target = np.zeros((height, width, 1), dtype=np.uint8)
    #     target[int(2 * height / 5):int(3 * height / 5), int(2 * width / 5):int(3 * width / 5)] = 255
    #     target2 = target
    #     while True:
    #         self.get_frame(once=True)
    #         hsv=cv2.cvtColor(self.frame,cv2.COLOR_BGR2HSV)
    #         hsv = cv2.bitwise_and(hsv, hsv, mask=target1)
    #         self.show_frame = cv2.bitwise_and(self.frame, self.frame, mask=target1)
    #         if cv2.waitKey(1) is 13:    # if key is enter
    #             for row in hsv:
    #                 for pixel in row:
    #                     # Goes through every pixel in the frame and finds the lowest and highest values
    #                     if not pixel.all() == 0:
    #                         if pixel[0] < minH:
    #                             minH = pixel[0]
    #                         if pixel[1] < minS:
    #                             minS = pixel[1]
    #                         if pixel[2] < minV:
    #                             minV = pixel[2]
    #                         if pixel[0] > maxH:
    #                             maxH = pixel[0]
    #                         if pixel[1] > maxS:
    #                             maxS = pixel[1]
    #                         if pixel[2] > maxV:
    #                             maxV = pixel[2]
    #             break
    #         self.show(once=True)
    #     # write the newfound values onto a file with the correct surfix
    #     file = open("files/Colors_" + self.surfix + ".val", 'w')
    #     file.write(
    #         "self.lower_range,self.upper_range = ({},{},{}),({},{},{})".format(minH, minS, minV, maxH, maxS, maxV))
    #     file.close()


    def range_finder(self):
        pass


    def check_files(self):
        try:
            _ = open("files/Colors_"+self.surfix+".val", 'r')
        except FileNotFoundError:
            self.range_finder()

        try:
            _ = open("files/Values_"+self.surfix+".val", 'r')
        except FileNotFoundError:
            file=open('files/Values_'+self.surfix+'.val','w+')
            file.write(
                'self.command_s = "area,0,0"\n'+
                'self.draw_contours_b = False\n'+
                'self.draw_hulls_b = False\n'+
                'self.dirode_iterations_i = 2\n'+
                'self.find_center_b = False\n'+
                'self._iterations_i=3\n'+
                'self.focal = 638.6086956521739\n'+
                'self.target_height = 0'
            )
            file.close()

    def init_values(self):
        color=open("files/Colors_"+self.surfix+".val",'r')
        exec(color.read())
        color.close()
        # Reads the latest values of the files
        values = open("files/Values_" + self.surfix + ".val", 'r')
        exec(values.read())
        values = values.readlines()
        for value in values:
            value = value.split("=")
            key = value[0].split(".")[1]
            value = eval(value[1])
            self.set_item(key, value)

    def set_range(self):
        # Retrieves the range written in "Ace" which was written there by Range Finder 3.0
        colors = open("files/Colors_"+self.surfix+".val", 'r')
        exec(colors.read())
        colors.close()

    def filter_hsv(self):
        self.hsv = cv2.cvtColor(self.frame, cv2.COLOR_BGR2HSV)
        self.mask = cv2.inRange(self.hsv, self.lower_range, self.upper_range)

    def draw_contours(self):
        # Draws contours on the frame, if asked so on SmartDashboard
        if len(self.contours) > 0 and self.get_item("Draw contours", self.draw_contours_b):
            # Works with indexes instead of the contours themselves
            for x in range(0, len(self.contours)):
                # Draws all contours in blue
                cv2.drawContours(self.show_frame, self.contours[x], -1, (255, 0, 0), 3)
                # Draws a green rectangle around the target.
                cv2.rectangle(self.show_frame,
                              (cv2.boundingRect(self.contours[x])[0], cv2.boundingRect(self.contours[x])[1]), (
                              cv2.boundingRect(self.contours[x])[0] + cv2.boundingRect(self.contours[x])[2],
                              cv2.boundingRect(self.contours[x])[1] + cv2.boundingRect(self.contours[x])[3]),
                              (0, 255, 0), 2)
                # Draws hulls on the frame, if asked so on SmartDashboard
                if len(self.hulls) > 0 and self.get_item("Draw hulls", self.draw_hulls_b):
                    # Finds all defects in the outline compared to the hull
                    defects = cv2.convexityDefects(self.contours[x], self.hulls[x])
                    for i in range(defects.shape[0]):
                        s, e, f, _ = defects[i, 0]
                        start = tuple(self.contours[x][s][0])
                        end = tuple(self.contours[x][e][0])
                        far = tuple(self.contours[x][f][0])
                        cv2.line(self.show_frame, start, end, [0, 255, 0], 2)
                        cv2.circle(self.show_frame, far, 5, [0, 0, 255], -1)

    def dirode(self):
        # Dialates and erodes the mask to reduce static and make the image clearer
        # The kernel both functions will use
        kernel = np.ones((5, 5), dtype=np.uint8)
        self.mask = cv2.erode(self.mask, kernel,
                              iterations=self.get_item("DiRode iterations", self.dirode_iterations_i))
        self.mask = cv2.dilate(self.mask, kernel,
                               iterations=self.get_item("DiRode iterations", self.dirode_iterations_i))

    def find_center(self):
        # Finds the average of all centers of all contours
        self.centers.clear()
        for i in range(0, len(self.contours)):
            # Uses the center of the minimum enclosing circle
            (x, y), _ = cv2.minEnclosingCircle(self.contours[i])
            self.centers.append((int(x), int(y)))
            # cv2.putText(self.show_frame, "o {}".format(self.centers[i]), self.centers[i], self.font, 0.5, 255)
            try:
                cv2.putText(self.show_frame, "o {}".format(self.center), self.center, self.font, 0.5, 255)
            except:
                pass

    def convexhull_triangle(self,c):
        hull_area=cv2.contourArea(cv2.convexHull(c))
        tl,tr,bl,br=self.rotated_cornecrs(c)

        temp_w=tl-tr
        width=math.sqrt(temp_w[0]**2+temp_w[1]**2)

        temp_h=tr-br
        height=math.sqrt(temp_h[0]**2+temp_h[1]**2)

        triangle_area=height*width/2
        return triangle_area/hull_area

    def convexhull_rectabgle(self,c):
        hull_area=cv2.contourArea(cv2.convexHull(c))
        tl,tr,bl,br=self.rotated_cornecrs(c)

        temp_w=tl-tr
        width=math.sqrt(temp_w[0]**2+temp_w[1]**2)

        temp_h=tr-br
        height=math.sqrt(temp_h[0]**2+temp_h[1]**2)

        rectangle_area=height*width
        return rectangle_area/hull_area

    def get_contours(self):
        # Executes a command line from SmartDashboard
        command = self.get_item("Command", self.command_s)
        # Splits the command into separate instructions
        functions = command.split(";")
        if len(functions) > 0:
            self.hulls.clear()
            for fun in functions:
                # Separates the instruction into method and margin
                fun = fun.split(",")
                key, min, max = fun[0], float(fun[1]), float(fun[2])
                # Creates a list of all appropriate contours
                possible_fit = []
                for c in self.contours:
                    method = getattr(vision, key)
                    if max > method(c) > min:
                        possible_fit.append(c)
                        if key == 'hull':
                            self.hulls.append(cv2.convexHull(c, returnPoints=False))
                # Updates the contour list
                self.contours = possible_fit

    def area(self, c):
        return cv2.contourArea(c)

    def extent(self, c):
        return cv2.contourArea(c) / (cv2.minAreaRect(c)[1][0] * cv2.minAreaRect(c)[1][1])

    def height(self, c):
        return cv2.boundingRect(c)[3]

    def hull(self, c):
        return cv2.contourArea(c) / cv2.contourArea(cv2.convexHull(c))

    def rotated_cornecrs(self,c):
        box=cv2.boxPoints(cv2.minAreaRect(c))
        box = list(box)
        bottom = []

        def sortbyindex1(arr):
            return arr[1]

        def sortbyindex0(arr):
            return arr[0]

        box.sort(key=sortbyindex1)
        bottom.append(box[2])
        bottom.append(box[3])

        box.pop(3)
        box.pop(2)
        top = box

        top.sort(key=sortbyindex0)
        bottom.sort(key=sortbyindex0)

        top_left = top[0]
        top_right = top[1]
        bottom_left = bottom[0]
        bottom_right = bottom[1]

        return top_left, top_right, bottom_left, bottom_right

    def aspectratio(self, c):
        top_left, top_right, bottom_left, bottom_right = self.rotated_cornecrs(c)

        temp_h = top_left-bottom_left
        temp_w = bottom_left-bottom_right

        height= math.sqrt(temp_h[0]**2+temp_h[1]**2)
        width= math.sqrt(temp_w[0]**2+temp_w[1]**2)

        return height / width

    def diameterratio(self, c):
        return (np.sqrt(4 * cv2.contourArea(c) / np.pi)) / (cv2.minEnclosingCircle(c)[1] * 2)

    def get_two(self):
        # gets the two closest contours
        dif = (10+(5.26))/41
        possible_fit = []
        for i in range(0, len(self.centers)-1):
            for j in range(i+1, len(self.centers)):
                _, y1, _, h1 = cv2.boundingRect(self.contours[i])
                _, y2, _, h2 = cv2.boundingRect(self.contours[j])
                h_av = abs((h1 + h2) / 2)
                dis = abs((self.centers[i][0] - self.centers[j][0]))
                hd=(dis/h_av)
                if dif * 0.75 < hd < dif * 1.25:  # <-- this specific line might be bugged and not tested yet so...
                    possible_fit = [self.contours[i], self.contours[j]]
                    self.center = int((self.centers[i][0] + self.centers[j][0])/2),(int((self.centers[j][1]+self.centers[i][1])/2))
                    dif = dis
        self.contours = possible_fit

    def get_angle(self):
        """
        Finds the Center of all the contours and then returns the angle using
        the focal length and the distance from the middle
        :return:
        angle from center
        """
        x_dif = self.center[0] - self.frame.shape[1]/2
        rad=math.atan(x_dif / self.focal)*-1 # because... math... apparently
        self.degrees=rad/math.pi*180
        self.set_item('Switch Degrees',self.degrees)
        return self.degrees


    def get_distance(self):
        """
        Finds the distance using the real target height and its pixel representation
        (similar triangles)
        :return:
        distance from targetself.frame.shape
        """
        height=0
        some=len(self.contours)
        for cont in self.contours:
            top_left, _, bottom_left, _ = self.rotated_cornecrs(cont)
            temp_h = top_left - bottom_left
            height += math.sqrt(temp_h[0] ** 2 + temp_h[1] ** 2)
        try:
            height/=some
        except ZeroDivisionError:
            pass
        try:
            distance = self.target_height * self.focal / height / 2
        except ZeroDivisionError:
            distance=None
        self.distance=distance
        self.set_item('Switch Distance',self.distance)
        return self.distance # for meters

    def gen(self):
        while not stop:
            jpg = cv2.imencode('.jpg', self.show_frame)[1].tostring()
            yield (b'--frame\r\n'b'Content-Type: image/jpeg\r\n\r\n' + jpg + b'\r\n')
            key = cv2.waitKey(1)

    def gen_mask(self):
        while not stop:
            jpg = cv2.imencode('.jpg', self.mask)[1].tostring()
            yield (b'--frame\r\n'b'Content-Type: image/jpeg\r\n\r\n' + jpg + b'\r\n')
            key = cv2.waitKey(1)

    def get_frame(self, once=False):
        if once:
            _, self.frame = self.cam.read()
            self.show_frame = self.frame.copy()
        else:
            global stop
            while not stop:
                _, self.frame = self.cam.read()
                self.show_frame = self.frame.copy()

    def show(self, once=False):
        global stop
        if is_stream:
            self.app.run(host=ip, debug=False)
        if is_local:
            while not stop:
                cv2.imshow('Frame', self.show_frame)
                if mask:
                    try:
                        cv2.imshow('Mask', self.mask)
                    except:
                        pass
                self.key = cv2.waitKey(1)
                if self.key is ord('q'):
                    cv2.destroyAllWindows()
                    stop = True
                if once:
                    break

    def analyse(self):
        global stop
        while not stop:
            if self.surfix is not self.get_item("Filter Mode",self.surfix):
                self.surfix=self.get_item("Filter Mode",self.surfix)
                self.check_files()
                self.init_values()
            self.filter_hsv()
            self.dirode()
            _, contours, _ = cv2.findContours(self.mask.copy(), cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)
            self.contours = list(contours)
            self.get_contours()
            if len(self.contours) > 0:
                self.find_center()
                if self.surfix is '0':
                    self.get_two()
                    if hasattr(self, 'center'):
                        self.sees_target = True
                        self.get_distance()
                        self.get_angle()
                else:
                    self.sees_target = True
                self.draw_contours()
            else:
                self.sees_target = False
            self.set_item("Sees target", self.sees_target)

# -----------Setting Global Variables For Thread-work----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

global vision
vision = Vision('0')

# ---------------Starting The Threads--------------------------------------------------------------------------------------------------
import threading

threading._start_new_thread(vision.get_frame, ())
threading._start_new_thread(vision.analyse, ())
vision.show()

# while not stop:
#    vision.get_frame()
#    vision.analyse()
#    vision.show()

if not is_local and not is_stream:
    _ = input('Press ' + colored.cyan('Enter') + ' To End It All')
stop = True
