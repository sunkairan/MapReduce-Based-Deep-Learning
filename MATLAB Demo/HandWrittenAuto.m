function varargout = HandWrittenAuto(varargin)
% HANDWRITTENAUTO MATLAB code for HandWrittenAuto.fig
%      HANDWRITTENAUTO, by itself, creates a new HANDWRITTENAUTO or raises the existing
%      singleton*.
%
%      H = HANDWRITTENAUTO returns the handle to a new HANDWRITTENAUTO or the handle to
%      the existing singleton*.
%
%      HANDWRITTENAUTO('CALLBACK',hObject,eventData,handles,...) calls the local
%      function named CALLBACK in HANDWRITTENAUTO.M with the given input arguments.
%
%      HANDWRITTENAUTO('Property','Value',...) creates a new HANDWRITTENAUTO or raises the
%      existing singleton*.  Starting from the left, property value pairs are
%      applied to the GUI before HandWrittenAuto_OpeningFcn gets called.  An
%      unrecognized property name or invalid value makes property application
%      stop.  All inputs are passed to HandWrittenAuto_OpeningFcn via varargin.
%
%      *See GUI Options on GUIDE's Tools menu.  Choose "GUI allows only one
%      instance to run (singleton)".
%
% See also: GUIDE, GUIDATA, GUIHANDLES

% Edit the above text to modify the response to help HandWrittenAuto

% Last Modified by GUIDE v2.5 24-Nov-2013 21:56:08

% Begin initialization code - DO NOT EDIT
gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
                   'gui_Singleton',  gui_Singleton, ...
                   'gui_OpeningFcn', @HandWrittenAuto_OpeningFcn, ...
                   'gui_OutputFcn',  @HandWrittenAuto_OutputFcn, ...
                   'gui_LayoutFcn',  [] , ...
                   'gui_Callback',   []);
if nargin && ischar(varargin{1})
    gui_State.gui_Callback = str2func(varargin{1});
end

if nargout
    [varargout{1:nargout}] = gui_mainfcn(gui_State, varargin{:});
else
    gui_mainfcn(gui_State, varargin{:});
end
% End initialization code - DO NOT EDIT


% --- Executes just before HandWrittenAuto is made visible.
function HandWrittenAuto_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to HandWrittenAuto (see VARARGIN)

% Choose default command line output for HandWrittenAuto
handles.output = hObject;

% Update handles structure
guidata(hObject, handles);

% UIWAIT makes HandWrittenAuto wait for user response (see UIRESUME)
% uiwait(handles.figure_hw);
set(handles.pushbutton_enc,'Enable','off');
set(handles.pushbutton_dec,'Enable','off');
set(handles.axes_src,'XColor','white');
set(handles.axes_src,'YColor','white');
set(handles.axes_src,'XTick',[]);
set(handles.axes_src,'YTick',[]);
set(handles.axes_des,'XColor','white');
set(handles.axes_des,'YColor','white');
set(handles.axes_des,'XTick',[]);
set(handles.axes_des,'YTick',[]);
set(handles.axes_code,'XColor','white');
set(handles.axes_code,'YColor','white');
set(handles.axes_code,'XTick',[]);
set(handles.axes_code,'YTick',[]);
userDraw(handles);

% --- Outputs from this function are returned to the command line.
function varargout = HandWrittenAuto_OutputFcn(hObject, eventdata, handles) 
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;


% --- Executes on button press in pushbutton_enc.
function pushbutton_enc_Callback(hObject, eventdata, handles)
% hObject    handle to pushbutton_enc (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

w1 = getappdata(handles.figure_hw,'w1');
w2 = getappdata(handles.figure_hw,'w2');
w3 = getappdata(handles.figure_hw,'w3');
w4 = getappdata(handles.figure_hw,'w4');


pix=getframe(handles.axes_src);
img=imresize(pix.cdata, [28,28]);
axes(handles.axes_src);
image(img);
set(handles.axes_src,'XColor','white');
set(handles.axes_src,'YColor','white');
set(handles.axes_src,'XTick',[]);
set(handles.axes_src,'YTick',[]);

w4probs = autoencode(img,w1,w2,w3,w4);

axes(handles.axes_code);
imshow(reshape(w4probs,[5,6]),[min(w4probs),max(w4probs)]);
set(handles.axes_code,'XColor','white');
set(handles.axes_code,'YColor','white');
set(handles.axes_code,'XTick',[]);
set(handles.axes_code,'YTick',[]);

% autodecode(w4probs,e5,w6,w7,w8);
setappdata(handles.figure_hw,'w4probs',w4probs);

set(handles.pushbutton_dec,'Enable','on');
userDraw(handles);
% set(handles.text_result,'String',num2str(digit));


% --------------------------------------------------------------------
function m_file_Callback(hObject, eventdata, handles)
% hObject    handle to m_file (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


% --------------------------------------------------------------------
function m_file_save_Callback(hObject, eventdata, handles)
% hObject    handle to m_file_save (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
pix=getframe(handles.axes_src);
[filename,pathname] = uiputfile({'*.bmp','BMP files';'*.jpg;','JPG files'},'Pick an Image'); 
if isequal(filename,0) || isequal(pathname,0) 
    return;% hit cancel
else
    fpath=fullfile(pathname,filename); % full file name
end
imwrite(imresize(pix.cdata, [28,28]),fpath);

% --------------------------------------------------------------------
function m_file_exit_Callback(hObject, eventdata, handles)
% hObject    handle to m_file_exit (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
close(handles.figure_hw);

function userDraw(handles)
%F=figure;
%setptr(F,'eraser'); %a custom cursor just for fun

A=handles.axes_src; % axesUserDraw is tag of my axes
set(A,'buttondownfcn',@start_pencil)

function start_pencil(src,eventdata)
coords=get(src,'currentpoint'); %since this is the axes callback, src=gca
x=coords(1,1,1);
y=coords(1,2,1);

r=line(x, y, 'color', [0 0 0], 'LineWidth', 9, 'hittest', 'off'); 
%turning     hittset off allows you to draw new lines that start on top of an existing line.
set(gcf,'windowbuttonmotionfcn',{@continue_pencil,r})
set(gcf,'windowbuttonupfcn',@done_pencil)

function continue_pencil(src,eventdata,r)
%Note: src is now the figure handle, not the axes, so we need to use gca.
coords=get(gca,'currentpoint'); %this updates every time i move the mouse
x=coords(1,1,1);
y=coords(1,2,1);
%get the line's existing coordinates and append the new ones.
lastx=get(r,'xdata');  
lasty=get(r,'ydata');
newx=[lastx x];
newy=[lasty y];
set(r,'xdata',newx,'ydata',newy);

function done_pencil(src,evendata)
%all this funciton does is turn the motion function off 
set(gcf,'windowbuttonmotionfcn','')
set(gcf,'windowbuttonupfcn','')



% --------------------------------------------------------------------
function m_file_open_Callback(hObject, eventdata, handles)
% hObject    handle to m_file_open (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
[filename,pathname] = uigetfile(... 
    {'*.mat','Weight File ( MATLAB format: *.mat )';... 
    '*.*','All Files (*.*)'},...
    'Pick an image');
if isequal(filename,0) || isequal(pathname,0) 
    return;% hit cancel
end
fpath = [pathname filename];
load(fpath);
setappdata(handles.figure_hw,'w1',w1);
setappdata(handles.figure_hw,'w2',w2);
setappdata(handles.figure_hw,'w3',w3);
setappdata(handles.figure_hw,'w4',w4);
setappdata(handles.figure_hw,'w5',w5);
setappdata(handles.figure_hw,'w6',w6);
setappdata(handles.figure_hw,'w7',w7);
setappdata(handles.figure_hw,'w8',w8);

set(handles.pushbutton_enc,'Enable','on');


function w4probs = autoencode(img,w1,w2,w3,w4)
    img = double(255 - rgb2gray(img))/255;
  data = reshape(img',1,28*28);

  data = [data 1];
  

  w1probs = 1./(1 + exp(-data*w1)); w1probs = [w1probs  1];
  w2probs = 1./(1 + exp(-w1probs*w2)); w2probs = [w2probs 1];
  w3probs = 1./(1 + exp(-w2probs*w3)); w3probs = [w3probs  1];
  w4probs = w3probs*w4;
  



% --- Executes on button press in pushbutton_clear.
function pushbutton_clear_Callback(hObject, eventdata, handles)
% hObject    handle to pushbutton_clear (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

cla 


% --- Executes on button press in pushbutton_dec.
function pushbutton_dec_Callback(hObject, eventdata, handles)
% hObject    handle to pushbutton_dec (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
w5 = getappdata(handles.figure_hw,'w5');
w6 = getappdata(handles.figure_hw,'w6');
w7 = getappdata(handles.figure_hw,'w7');
w8 = getappdata(handles.figure_hw,'w8');
w4probs = getappdata(handles.figure_hw,'w4probs');
img = autodecode(w4probs,w5,w6,w7,w8);
axes(handles.axes_des);
imshow(img,[0,1]);
set(handles.axes_des,'XColor','white');
set(handles.axes_des,'YColor','white');
set(handles.axes_des,'XTick',[]);
set(handles.axes_des,'YTick',[]);


  
function  dataout = autodecode(w4probs, w5,w6,w7,w8)

  w4probs = [w4probs  1];
  w5probs = 1./(1 + exp(-w4probs*w5)); w5probs = [w5probs 1];
  w6probs = 1./(1 + exp(-w5probs*w6)); w6probs = [w6probs 1];
  w7probs = 1./(1 + exp(-w6probs*w7)); w7probs = [w7probs 1];
  dataout = 1 - 1./(1 + exp(-w7probs*w8));
  dataout = (reshape(dataout,[28,28]))';

  
  
