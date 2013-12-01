function varargout = HandWrittenSuper(varargin)
% HANDWRITTENSUPER MATLAB code for HandWrittenSuper.fig
%      HANDWRITTENSUPER, by itself, creates a new HANDWRITTENSUPER or raises the existing
%      singleton*.
%
%      H = HANDWRITTENSUPER returns the handle to a new HANDWRITTENSUPER or the handle to
%      the existing singleton*.
%
%      HANDWRITTENSUPER('CALLBACK',hObject,eventData,handles,...) calls the local
%      function named CALLBACK in HANDWRITTENSUPER.M with the given input arguments.
%
%      HANDWRITTENSUPER('Property','Value',...) creates a new HANDWRITTENSUPER or raises the
%      existing singleton*.  Starting from the left, property value pairs are
%      applied to the GUI before HandWrittenSuper_OpeningFcn gets called.  An
%      unrecognized property name or invalid value makes property application
%      stop.  All inputs are passed to HandWrittenSuper_OpeningFcn via varargin.
%
%      *See GUI Options on GUIDE's Tools menu.  Choose "GUI allows only one
%      instance to run (singleton)".
%
% See also: GUIDE, GUIDATA, GUIHANDLES

% Edit the above text to modify the response to help HandWrittenSuper

% Last Modified by GUIDE v2.5 20-Nov-2013 03:00:19

% Begin initialization code - DO NOT EDIT
gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
                   'gui_Singleton',  gui_Singleton, ...
                   'gui_OpeningFcn', @HandWrittenSuper_OpeningFcn, ...
                   'gui_OutputFcn',  @HandWrittenSuper_OutputFcn, ...
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


% --- Executes just before HandWrittenSuper is made visible.
function HandWrittenSuper_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to HandWrittenSuper (see VARARGIN)

% Choose default command line output for HandWrittenSuper
handles.output = hObject;

% Update handles structure
guidata(hObject, handles);

% UIWAIT makes HandWrittenSuper wait for user response (see UIRESUME)
% uiwait(handles.figure_hw);
set(handles.pushbutton_recog,'Enable','off');
userDraw(handles);

% --- Outputs from this function are returned to the command line.
function varargout = HandWrittenSuper_OutputFcn(hObject, eventdata, handles) 
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;


% --- Executes on button press in pushbutton_recog.
function pushbutton_recog_Callback(hObject, eventdata, handles)
% hObject    handle to pushbutton_recog (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

w1 = getappdata(handles.figure_hw,'w1');
w2 = getappdata(handles.figure_hw,'w2');
w3 = getappdata(handles.figure_hw,'w3');
w_class = getappdata(handles.figure_hw,'w_class');

pix=getframe(handles.axes_src);
img=imresize(pix.cdata, [28,28]);
digit = recognize(img,w1,w2,w3,w_class);
set(handles.text_result,'String',num2str(digit));


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
setappdata(handles.figure_hw,'w_class',w_class);

set(handles.pushbutton_recog,'Enable','on');


function digit = recognize(img,w1,w2,w3,w_class)
    img = double(255 - rgb2gray(img))/255;
  data = reshape(img',1,28*28);

  data = [data 1];
  w1probs = 1./(1 + exp(-data*w1)); w1probs = [w1probs  1];
  w2probs = 1./(1 + exp(-w1probs*w2)); w2probs = [w2probs 1];
  w3probs = 1./(1 + exp(-w2probs*w3)); w3probs = [w3probs 1];
  targetout = exp(w3probs*w_class);
  targetout = targetout./repmat(sum(targetout),1,10);
  maxdigit = max(targetout);
  for i = (0:9)
      if targetout(i+1)==maxdigit
          digit = i;
      end
  end


% --- Executes on button press in pushbutton_clear.
function pushbutton_clear_Callback(hObject, eventdata, handles)
% hObject    handle to pushbutton_clear (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

cla 
