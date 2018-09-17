function varargout = aux_sliceViewer(varargin)
% AUX_SLICEVIEWER MATLAB code for aux_sliceViewer.fig
%
%      Visualize the measurement and deconvolved image.
%
%      AUX_SLICEVIEWER, by itself, creates a new AUX_SLICEVIEWER or raises the existing
%      singleton*.
%
%      H = AUX_SLICEVIEWER returns the handle to a new AUX_SLICEVIEWER or the handle to
%      the existing singleton*.
%
%      AUX_SLICEVIEWER('CALLBACK',hObject,eventData,handles,...) calls the local
%      function named CALLBACK in AUX_SLICEVIEWER.M with the given input arguments.
%
%      AUX_SLICEVIEWER('Property','Value',...) creates a new AUX_SLICEVIEWER or raises the
%      existing singleton*.  Starting from the left, property value pairs are
%      applied to the GUI before aux_sliceViewer_OpeningFcn gets called.  An
%      unrecognized property name or invalid value makes property application
%      stop.  All inputs are passed to aux_sliceViewer_OpeningFcn via varargin.
%
%      *See GUI Options on GUIDE's Tools menu.  Choose "GUI allows only one
%      instance to run (singleton)".
%
% See also: GUIDE, GUIDATA, GUIHANDLES

% Edit the above text to modify the response to help aux_sliceViewer

% Author: Jizhou Li (hijizhou@gmail.com)
%         Department of Electronic Engineering
%         The Chinese University of Hong Kong
%
% References:
%     [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution, 
%           IEEE Trans. Image Process., vol. 27, no. 1, pp. 92-105, 2018.
%     [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 
%           2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP 2016), Phoenix, Arizona, USA, 2016, pp.2708-2712.
%     [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 
%           2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI 2017), Melbourne, Australia, 2017, pp. 723-727.
%
% Last Modified on 09 Nov, 2017
%

% Begin initialization code - DO NOT EDIT
gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
                   'gui_Singleton',  gui_Singleton, ...
                   'gui_OpeningFcn', @aux_sliceViewer_OpeningFcn, ...
                   'gui_OutputFcn',  @aux_sliceViewer_OutputFcn, ...
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


% --- Executes just before aux_sliceViewer is made visible.
function aux_sliceViewer_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to aux_sliceViewer (see VARARGIN)

% Choose default command line output for aux_sliceViewer
handles.output = hObject;

% Update handles structure
guidata(hObject, handles);

% UIWAIT makes aux_sliceViewer wait for user response (see UIRESUME)
% uiwait(handles.figure1);

if (length(varargin) <=0)
    error('Input objects have not been specified.');
end;
input = varargin{1};
output = varargin{2};
if (ndims(input) ~= 3||ndims(output) ~=3)
    error('Input objects must have 3 dimensions.');
end;
if size(input)~=size(output)
    error('Input and output should have the same dimensions.');
end;

handles.input = input;
handles.output = output;

% set main wnd title
set(gcf, 'Name', 'Deconvolution Slice Viewer (Click to change the cross location)')
% init 3D pointer
vol_sz = size(input); 
handles.axis_equal = 1;
handles.colormap = 'green'; %default green channel

pointer3dt = floor(vol_sz/2)+1;
handles.pointer3dt = pointer3dt;
handles.vol_sz = vol_sz;

plot3slices(hObject, handles);

% stores ID of last axis window 
% (0 means that no axis was clicked yet)
handles.last_axis_id = 0;

% Update handles structure
guidata(hObject, handles);

% --- Plots all 3 slices XY, YZ, XZ into 3 subplots
function [sp1,sp2,sp3] = plot3slices(hObject, handles)
% pointer3d     3D coordinates in volume matrix (integers)

handles.pointer3dt;
size(handles.input);
% value3dt = handles.input(handles.pointer3dt(1), handles.pointer3dt(2), handles.pointer3dt(3), handles.pointer3dt(4));

% text_str = ['[X:' int2str(handles.pointer3dt(1)) ...
%            ', Y:' int2str(handles.pointer3dt(2)) ...
%            ', Z:' int2str(handles.pointer3dt(3)) ...
%            ', Time:' int2str(handles.pointer3dt(4)) '/' int2str(handles.vol_sz(4)) ...
%            '], value:' num2str(value3dt)];
% set(handles.pointer3d_info, 'String', text_str);
guidata(hObject, handles);

handles.color_mode = 1;

sliceXY_input = squeeze(handles.input(:,:,handles.pointer3dt(3),:));
    sliceYZ_input = squeeze(handles.input(handles.pointer3dt(1),:,:,:));
    sliceXZ_input = squeeze(handles.input(:,handles.pointer3dt(2),:,:));

    sliceXY_output = squeeze(handles.output(:,:,handles.pointer3dt(3),:));
    sliceYZ_output = squeeze(handles.output(handles.pointer3dt(1),:,:,:));
    sliceXZ_output = squeeze(handles.output(:,handles.pointer3dt(2),:,:));
    
    max_xyz_input = max([ max(sliceXY_input(:)) max(sliceYZ_input(:)) max(sliceXZ_input(:)) ]);
    max_xyz_output = max([ max(sliceXY_output(:)) max(sliceYZ_output(:)) max(sliceXZ_output(:)) ]);
    
    min_xyz_input = min([ min(sliceXY_input(:)) min(sliceYZ_input(:)) min(sliceXZ_input(:)) ]);
    min_xyz_output = min([ min(sliceXY_output(:)) min(sliceYZ_output(:)) min(sliceXZ_output(:)) ]);
    
    clims_input = [ min_xyz_input max_xyz_input ];
    clims_output = [ min_xyz_output max_xyz_output ];

    
sliceZY_input = squeeze(permute(sliceYZ_input, [2 1 3]));
sliceZY_output = squeeze(permute(sliceYZ_output, [2 1 3]));

% sp1 = subplot(2,2,1);
axes(handles.axes1);
%colorbar;
imshow(sliceXY_input,clims_input);
aux_colorspec(handles.colormap);
% title('Slice XY');
% ylabel('X');xlabel('Y');
line([handles.pointer3dt(2) handles.pointer3dt(2)], [0 size(handles.input,1)],'LineWidth',1,'Color',[1 1 0]);
line([0 size(handles.input,2)], [handles.pointer3dt(1) handles.pointer3dt(1)],'LineWidth',1,'Color',[1 1 0]);
    axis normal;
set(allchild(gca),'ButtonDownFcn','aux_sliceViewer(''Subplot1_ButtonDownFcn'',gca,[],guidata(gcbo))');

axes(handles.axes4);
%colorbar;
imshow(sliceXY_output,clims_output);
aux_colorspec(handles.colormap);

% title('Slice XY');
% ylabel('X');xlabel('Y');
line([handles.pointer3dt(2) handles.pointer3dt(2)], [0 size(handles.output,1)],'LineWidth',1,'Color',[1 1 0]);
line([0 size(handles.output,2)], [handles.pointer3dt(1) handles.pointer3dt(1)],'LineWidth',1,'Color',[1 1 0]);
    axis normal;
set(allchild(gca),'ButtonDownFcn','aux_sliceViewer(''Subplot1_ButtonDownFcn'',gca,[],guidata(gcbo))');

    
axes(handles.axes2);
imshow(sliceXZ_input, clims_input);
aux_colorspec(handles.colormap);

title('Slice YZ');
% ylabel('X');xlabel('Z');
line([handles.pointer3dt(3) handles.pointer3dt(3)], [0 size(handles.input,1)],'LineWidth',1,'Color',[1 1 0]);
line([0 size(handles.input,3)], [handles.pointer3dt(1) handles.pointer3dt(1)],'LineWidth',1,'Color',[1 1 0]);
set(allchild(gca),'ButtonDownFcn','aux_sliceViewer(''Subplot2_ButtonDownFcn'',gca,[],guidata(gcbo))');
if (handles.axis_equal == 1)
    axis image;
else
    axis normal;
end;

axes(handles.axes5);
imshow(sliceXZ_output, clims_output);
aux_colorspec(handles.colormap);

title('Slice YZ');
% ylabel('X');xlabel('Z');
line([handles.pointer3dt(3) handles.pointer3dt(3)], [0 size(handles.output,1)],'LineWidth',1,'Color',[1 1 0]);
line([0 size(handles.output,3)], [handles.pointer3dt(1) handles.pointer3dt(1)],'LineWidth',1,'Color',[1 1 0]);
set(allchild(gca),'ButtonDownFcn','aux_sliceViewer(''Subplot2_ButtonDownFcn'',gca,[],guidata(gcbo))');
if (handles.axis_equal == 1)
    axis image;
else
    axis normal;
end;


axes(handles.axes3);
imshow(sliceZY_input, clims_input);
aux_colorspec(handles.colormap);

title('Slice XZ');
% ylabel('Z');xlabel('Y');
line([0 size(handles.input,2)], [handles.pointer3dt(3) handles.pointer3dt(3)],'LineWidth',1,'Color',[1 1 0]);
line([handles.pointer3dt(2) handles.pointer3dt(2)], [0 size(handles.input,3)],'LineWidth',1,'Color',[1 1 0]);
set(allchild(gca),'ButtonDownFcn','aux_sliceViewer(''Subplot3_ButtonDownFcn'',gca,[],guidata(gcbo))');
if (handles.axis_equal == 1)
    axis image;
else
    axis normal;
end;
axes(handles.axes6);
imshow(sliceZY_output, clims_output);
aux_colorspec(handles.colormap);

title('Slice XZ');
% ylabel('Z');xlabel('Y');
line([0 size(handles.output,2)], [handles.pointer3dt(3) handles.pointer3dt(3)],'LineWidth',1,'Color',[1 1 0]);
line([handles.pointer3dt(2) handles.pointer3dt(2)], [0 size(handles.output,3)],'LineWidth',1,'Color',[1 1 0]);
set(allchild(gca),'ButtonDownFcn','aux_sliceViewer(''Subplot3_ButtonDownFcn'',gca,[],guidata(gcbo))');
if (handles.axis_equal == 1)
    axis image;
else
    axis normal;
end;

% --- Executes on mouse press over axes background.
function Subplot1_ButtonDownFcn(hObject, eventdata, handles)
% hObject    handle to Subplot1 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% This object contains the XY slice

%disp('Subplot1:BtnDown');
pt=get(gca,'currentpoint');
xpos=round(pt(1,2)); ypos=round(pt(1,1));
zpos = handles.pointer3dt(3);
handles.pointer3dt = [xpos ypos zpos];
handles.pointer3dt = clipointer3d(handles.pointer3dt,handles.vol_sz);
plot3slices(hObject, handles);
% store this axis as last clicked region
handles.last_axis_id = 1;
% Update handles structure
guidata(hObject, handles);

% --- Executes on mouse press over axes background.
function Subplot2_ButtonDownFcn(hObject, eventdata, handles)
% hObject    handle to Subplot2 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% This object contains the YZ slice

%disp('Subplot2:BtnDown');
pt=get(gca,'currentpoint');
xpos=round(pt(1,2)); zpos=round(pt(1,1));
ypos = handles.pointer3dt(2);
handles.pointer3dt = [xpos ypos zpos];
handles.pointer3dt = clipointer3d(handles.pointer3dt,handles.vol_sz);
plot3slices(hObject, handles);
% store this axis as last clicked region
handles.last_axis_id = 2;
% Update handles structure
guidata(hObject, handles);

% --- Executes on mouse press over axes background.
function Subplot3_ButtonDownFcn(hObject, eventdata, handles)
% hObject    handle to Subplot3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% This object contains the XZ slice

%disp('Subplot3:BtnDown');
pt=get(gca,'currentpoint');
zpos=round(pt(1,2)); ypos=round(pt(1,1));
xpos = handles.pointer3dt(1);
handles.pointer3dt = [xpos ypos zpos];
handles.pointer3dt = clipointer3d(handles.pointer3dt,handles.vol_sz);
plot3slices(hObject, handles);
% store this axis as last clicked region
handles.last_axis_id = 3;
% Update handles structure
guidata(hObject, handles);


% --- Outputs from this function are returned to the command line.
function varargout = aux_sliceViewer_OutputFcn(hObject, eventdata, handles) 
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;

function pointer3d_out = clipointer3d(pointer3d_in,vol_size)
pointer3d_out = pointer3d_in;
for p_id=1:3
    if (pointer3d_in(p_id) > vol_size(p_id))
        pointer3d_out(p_id) = vol_size(p_id);
    end;
    if (pointer3d_in(p_id) < 1)
        pointer3d_out(p_id) = 1;
    end;
end;
