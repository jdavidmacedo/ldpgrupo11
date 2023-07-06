create database game;
use game;
create table player1_tiles(
id int primary key auto_increment,
value1 int,
value2 int
);
create table player2_tiles(
id int primary key auto_increment,
value1 int,
value2 int
);
create table game_tiles(
id int primary key auto_increment,
value1 int,
value2 int
);
create table player_scores(
player varchar(60) primary key,
score int
);
create table active(
player int primary key,
active int 
);
create table current(
curr int primary key);
insert into current values(1);

Insert into player_scores values ('Player 1',0);
Insert into player_scores values ('Player 2',0);






